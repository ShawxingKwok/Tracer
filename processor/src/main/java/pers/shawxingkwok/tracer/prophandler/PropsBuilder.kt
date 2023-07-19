package pers.shawxingkwok.tracer.prophandler

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.ktutil.updateIf
import pers.shawxingkwok.tracer.shared.Names
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getSrcKlassTraceableSuperTypes
import pers.shawxingkwok.tracer.typesystem.getTraceableTypes
import pers.apollokwok.tracer.common.util.*
import pers.apollokwok.tracer.util.*
import pers.shawxingkwok.tracer.util.SUPPRESSING
import pers.shawxingkwok.tracer.util.getPreNeededProperties
import pers.shawxingkwok.tracer.util.isAnnotatedRootOrNodes
import pers.shawxingkwok.tracer.util.moduleVisibility
import pers.shawxingkwok.tracer.util.limitVisibility
import pers.shawxingkwok.tracer.util.trimMarginAndRepeatedBlankLines

internal class PropsBuilder(val srcKlass: KSClassDeclaration) {
    private val record = object {
        val validlyTracedInsideKlasses: Set<KSClassDeclaration> = mutableSetOf()
        val tracedKlassesStoppedTracingInsideForNullability: Set<KSClassDeclaration> = mutableSetOf()

        fun getErrorMsg(klass: KSClassDeclaration) =
            "Annotate $klass with ${Names.Nodes} or ${Names.Tip} since it appeared in " +
            "$srcKlass for multiple times and it is in the current module with some " +
            "not omitted visible properties to trace."
    }

    // collect sourceKlass superTypes in a mutable list
    private val newPropsInfo: MutableList<PropInfo> =
        getSrcKlassTraceableSuperTypes(srcKlass)
        .mapNotNull { type ->
            PropInfo.FromSrcKlassSuper(
                klass = srcKlass,
                type = type,
                v = limitVisibility(
                    srcKlass.moduleVisibility(),
                    *type.allInnerKlasses.map { it.moduleVisibility() }.toTypedArray(),
                    if (Tags.AllInternal) Visibility.INTERNAL else Visibility.PUBLIC
                ) ?: return@mapNotNull null,
                propsBuilder = this
            )
        }
        .toMutableList()

    private fun trace(klass: KSClassDeclaration, parentProp: KSPropertyDeclaration?, lastV: Visibility){
        klass.getPreNeededProperties()
            .asSequence()
            .map { prop -> prop to prop.getTraceableTypes() }
            // record
            .onEachIndexed{ i, _->
                if (i != 0) return@onEachIndexed

                Log.require(
                    condition = klass !in record.validlyTracedInsideKlasses
                                && klass !in record.tracedKlassesStoppedTracingInsideForNullability,
                    symbols = listOfNotNull(parentProp, klass)
                ){
                    record.getErrorMsg(klass)
                }

                (record.validlyTracedInsideKlasses as MutableSet) += klass
            }
            // cache
            .onEach { (prop, types)->
                // the basic type must be visible, so the requirement in just 'onEachIndexed' must be valid.
                types.forEachIndexed { i, type ->
                    val v = limitVisibility(
                        prop.getVisibility(),
                        *type.allInnerKlasses.map { it.moduleVisibility() }.toTypedArray(),
                        lastV,
                    ) ?: return@forEachIndexed

                    // todo: handle `private set`
                    val mutable = false

//                        prop.isMutable
//                                  && prop.setter?.
//                        && i == 0
//                        && !(klass == srcKlass
//                            && srcKlass.typeParameters.any()
//                            && kotlin.run {
//                                fun KSTypeReference.containT(): Boolean =
//                                    resolve().declaration is KSTypeParameter
//                                    || element?.typeArguments?.any { it.type?.containT() == true } == true
//
//                                prop.type.containT()
//                            })

                    newPropsInfo += PropInfo.FromElement(
                        prop = prop,
                        parentProp = parentProp,
                        mutable = mutable,
                        type = type,
                        v = v,
                        propsBuilder = this
                    )
                }
            }
            // filter and trace inside, other filtering conditions are in 'getPreNeededProperties'
            .mapNotNull { (prop, types)->
                val basicType = types.first() as? Type.Specific ?: return@mapNotNull null
                prop to basicType
            }
            .filterNot { (_, basicType)-> basicType.decl.isAnnotatedRootOrNodes() }
            .filterNot { (prop, basicType)->
                if (basicType.nullable) {
                    Log.require(
                        condition = basicType.decl !in record.validlyTracedInsideKlasses,
                        symbols = listOf(prop, klass),
                    ){
                        record.getErrorMsg(basicType.decl)
                    }
                    (record.tracedKlassesStoppedTracingInsideForNullability as MutableSet) += basicType.decl
                }

                basicType.nullable
            }
            .forEach { (prop, basicType)->
                trace(
                    klass = basicType.decl,
                    parentProp = prop,
                    lastV = limitVisibility(prop.moduleVisibility(), lastV)!!
                )
            }
    }

    // start tracing inner property types in recurse in new props
    init {
        trace(
            klass = srcKlass,
            parentProp = null,
            lastV = srcKlass.moduleVisibility()!!.updateIf({ Tags.AllInternal }){ Visibility.INTERNAL }
        )
    }

    // type is omitted when compoundTypeSupported
    val imports = Imports(
        srcDecl = srcKlass,
        klasses = newPropsInfo.flatMap {
            if(it.compoundTypeSupported)
                emptyList()
            else
                it.type.allInnerKlasses
        }
    )

    private val builtTimesComment: String =
        //region
        newPropsInfo
        .groupingBy { it.type.getName(true) }
        .eachCount()
        .toList()
        .groupingBy { (_, times)-> times }
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
            prefix = "//region Below are simplified types with its built times inside `${srcKlass.noPackageName()!!}`.\n/*\n",
            separator = "\n\n",
            postfix = "\n*/\n//endregion",
        )
        //endregion

    // create file
    init{
        Environment.codeGenerator.createFile(
            packageName = srcKlass.packageName(),
            fileName = "${srcKlass.noPackageName()}Elements",
            dependencies = Dependencies(false, srcKlass.containingFile!!),
            content = """
                |$SUPPRESSING
                |
                |${if (srcKlass.packageName().any()) "package ${srcKlass.packageName()}" else "" }
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