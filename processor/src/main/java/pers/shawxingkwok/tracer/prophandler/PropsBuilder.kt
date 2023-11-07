package pers.shawxingkwok.tracer.prophandler

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.tracer.shared.Names
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getSrcKSClassTraceableSuperTypes
import pers.shawxingkwok.tracer.typesystem.getTraceableTypes
import pers.shawxingkwok.tracer.util.SUPPRESSING
import pers.shawxingkwok.tracer.util.getPreNeededKSProperties
import pers.shawxingkwok.tracer.util.isAnnotatedRootOrNodes
import pers.shawxingkwok.tracer.util.moduleVisibility
import pers.shawxingkwok.tracer.util.limitVisibility
import pers.shawxingkwok.tracer.util.trimMarginAndRepeatedBlankLines

internal class PropsBuilder(val srcKSClass: KSClassDeclaration) {
    private val record = object {
        val validlyTracedInsideKSClasses: Set<KSClassDeclaration> = mutableSetOf()
        val tracedKSClassesStoppedTracingInsideForNullability: Set<KSClassDeclaration> = mutableSetOf()

        fun getErrorMsg(ksClass: KSClassDeclaration) =
            "Annotate $ksClass with ${Names.Nodes} or ${Names.Tip} since it appeared in " +
            "$srcKSClass for multiple times and it is in the current module with some " +
            "not omitted visible properties to trace."
    }

    // collect sourceKSClass superTypes in a mutable list
    private val newPropsInfo: MutableList<PropInfo> =
        getSrcKSClassTraceableSuperTypes(srcKSClass)
        .mapNotNull { type ->
            PropInfo.FromSrcKSClassSuper(
                ksClass = srcKSClass,
                type = type,
                v = limitVisibility(
                    srcKSClass.moduleVisibility(),
                    *type.allInnerKSClasses.map { it.moduleVisibility() }.toTypedArray(),
                    if (Tags.AllInternal) Visibility.INTERNAL else Visibility.PUBLIC
                ) ?: return@mapNotNull null,
                propsBuilder = this
            )
        }
        .toMutableList()

    private fun trace(ksClass: KSClassDeclaration, parentKSProp: KSPropertyDeclaration?, lastV: Visibility){
        ksClass.getPreNeededKSProperties()
            .asSequence()
            .map { prop -> prop to prop.getTraceableTypes() }
            // record
            .onEachIndexed{ i, _->
                if (i != 0) return@onEachIndexed

                Log.check(
                    symbols = listOfNotNull(parentKSProp, ksClass),
                    condition = ksClass !in record.validlyTracedInsideKSClasses
                                && ksClass !in record.tracedKSClassesStoppedTracingInsideForNullability,
                ){
                    record.getErrorMsg(ksClass)
                }

                (record.validlyTracedInsideKSClasses as MutableSet) += ksClass
            }
            // cache
            .onEach { (prop, types) ->
                // the basic type must be visible, so the requirement in just 'onEachIndexed' must be valid.
                types.forEachIndexed { i, type ->
                    val v = limitVisibility(
                        prop.getVisibility(),
                        *type.allInnerKSClasses.map { it.moduleVisibility() }.toTypedArray(),
                        lastV,
                    ) ?: return@forEachIndexed

                    // todo: handle `private set`
                    val mutable = false

//                        prop.isMutable
//                                  && prop.setter?.
//                        && i == 0
//                        && !(ksClass == srcKSClass
//                            && srcKSClass.typeParameters.any()
//                            && kotlin.run {
//                                fun KSTypeReference.containT(): Boolean =
//                                    resolve().declaration is KSTypeParameter
//                                    || element?.typeArguments?.any { it.type?.containT() ?: false } ?: false
//
//                                prop.type.containT()
//                            })

                    newPropsInfo += PropInfo.FromElement(
                        ksProp = prop,
                        parentKSProp = parentKSProp,
                        isMutable = mutable,
                        type = type,
                        v = v,
                        propsBuilder = this
                    )
                }
            }
            // filter and trace inside, other filtering conditions are in 'getPreNeededProperties'
            .mapNotNull { (ksProp, types) ->
                val basicType = types.first() as? Type.Specific ?: return@mapNotNull null
                ksProp to basicType
            }
            .filterNot { (_, basicType) -> basicType.ksClass.isAnnotatedRootOrNodes() }
            .filterNot { (ksProp, basicType) ->
                if (basicType.isNullable) {
                    Log.check(
                        symbols = listOf(ksProp, ksClass),
                        condition = basicType.ksClass !in record.validlyTracedInsideKSClasses,
                    ){
                        record.getErrorMsg(basicType.ksClass)
                    }
                    (record.tracedKSClassesStoppedTracingInsideForNullability as MutableSet) += basicType.ksClass
                }

                basicType.isNullable
            }
            .forEach { (prop, basicType) ->
                trace(
                    ksClass = basicType.ksClass,
                    parentKSProp = prop,
                    lastV = limitVisibility(prop.moduleVisibility(), lastV)!!
                )
            }
    }

    // start tracing inner property types in recurse in new props
    init {
        trace(
            ksClass = srcKSClass,
            parentKSProp = null,
            lastV = srcKSClass.moduleVisibility()!!.updateIf({ Tags.AllInternal }){ Visibility.INTERNAL }
        )
    }

    // type is omitted when compoundTypeSupported
    val imports = Imports(
        packageName = srcKSClass.packageName(),
        ksClasses = newPropsInfo.flatMap {
            if(it.compoundTypeSupported)
                emptyList()
            else
                it.type.allInnerKSClasses
        }
    )

    private val builtTimesComment: String =
        //region
        newPropsInfo
        .groupingBy { it.type.getName(true) }
        .eachCount()
        .toList()
        .groupingBy { (_, times) -> times }
        .aggregate<_,_,StringBuilder> { _, accumulator, (grossKey, times), _ ->
            val strBuilder = accumulator ?: java.lang.StringBuilder()
            when{
                accumulator != null -> ", "
                times == 1 -> "1 time: "
                else -> "$times times: "
            }
            .let(strBuilder::append)

            if (strBuilder.length + grossKey.length - strBuilder.lastIndexOf('\n') > 100) {
                strBuilder.append("\n        ")
                if (times > 1) strBuilder.append(" ")
            }
            strBuilder.append(grossKey)
        }
        .toSortedMap()
        .values
        .joinToString(
            prefix = "//region Below are simplified types with its built times inside `${srcKSClass.noPackageName()!!}`.\n/*\n",
            separator = "\n\n",
            postfix = "\n*/\n//endregion",
        )
        //endregion

    // create file
    init{
        Environment.codeGenerator.createFile(
            packageName = srcKSClass.packageName(),
            fileName = "${srcKSClass.noPackageName()}Elements",
            dependencies = Dependencies(false, srcKSClass.containingFile!!),
            content = """
                |$SUPPRESSING
                |
                |${if (srcKSClass.packageName().any()) "package ${srcKSClass.packageName()}" else "" }
                |
                |$imports
                |
                |$builtTimesComment
                |
                |${ newPropsInfo.joinToString("\n") { it.declContent } }
                |
                |// outer part
                |${ newPropsInfo.joinToString("\n") { it.outerDeclContent } }
                """
                .trimMarginAndRepeatedBlankLines()
        )
    }
}