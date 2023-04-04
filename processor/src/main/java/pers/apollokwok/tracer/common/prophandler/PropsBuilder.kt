package pers.apollokwok.tracer.common.prophandler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.shared.Names
import pers.apollokwok.tracer.common.shared.Names.GENERATED_PACKAGE
import pers.apollokwok.tracer.common.shared.Tags.AllInternal
import pers.apollokwok.tracer.common.shared.contractedName
import pers.apollokwok.tracer.common.shared.outermostDecl
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.getSrcKlassTraceableSuperTypes
import pers.apollokwok.tracer.common.typesystem.getTraceableTypes
import pers.apollokwok.tracer.common.util.*

internal class PropsBuilder(val srcKlass: KSClassDeclaration) {
    private val record = object {
        val validlyTracedInsideKlasses: Set<KSClassDeclaration> = mutableSetOf()
        val tracedKlassesStoppedTracingInsideForNullability: Set<KSClassDeclaration> = mutableSetOf()

        fun getErrorMsg(klass: KSClassDeclaration) =
            "Annotate $klass with ${Names.Nodes} or ${Names.Tip} since it appeared in " +
            "$srcKlass for multiple times and it is in the current module with some " +
            "not omitted visible properties to trace."
    }

    private fun getV(decl: KSDeclaration, type: Type<*>): Visibility?{
        val limitedV = limitVisibility(decl, *type.allInnerKlasses.toTypedArray())

        return when {
            limitedV == null -> null
            AllInternal -> Visibility.INTERNAL
            else -> limitedV
        }
    }

    // collect sourceKlass superTypes in a mutable list
    private val newPropsInfo: MutableList<PropInfo> =
        getSrcKlassTraceableSuperTypes(srcKlass)
        .mapNotNull { type ->
            PropInfo.FromSrcKlassSuper(
                klass = srcKlass,
                type = type,
                v = getV(srcKlass, type) ?: return@mapNotNull null,
                propsBuilder = this
            )
        }
        .toMutableList()

    private fun trace(klass: KSClassDeclaration, parentProp: KSPropertyDeclaration?){
        klass.getPreNeededProperties()
            .asSequence()
            // Omit Any and warn
            .filterNot { prop->
                val isAny = prop.type.resolve().declaration == Type.`Any？`.decl

                if (isAny)
                    Log.w(
                        msg = "This property would be omitted because its type is Any. " +
                            "You can annotate this property with ${Names.Omit} to make it look clear.",
                        prop
                    )

                isAny
            }
            .map { prop -> prop to prop.getTraceableTypes() }
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
                    val v = getV(prop, type) ?: return@forEachIndexed

                    val mutable = prop.isMutable
                        && i == 0
                        && !(klass == srcKlass
                            && srcKlass.typeParameters.any()
                            && kotlin.run {
                                fun KSTypeReference.containT(): Boolean =
                                    resolve().declaration is KSTypeParameter
                                    || element?.typeArguments?.any { it.type?.containT() == true } == true

                                prop.type.containT()
                            })

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
                    parentProp = prop
                )
            }
    }

    // start tracing inner property types in recurse in new props
    init {
        trace(srcKlass, null)
    }

    private val allInnerKlasses = newPropsInfo.flatMap { it.type.allInnerKlasses }.toSet()

    val importedOutermostKlasses = allInnerKlasses
        //region
        .map { it.outermostDecl }
        .groupBy { it.simpleName() }
        .mapNotNull { (_, similarKlasses)->
            similarKlasses.singleOrNull() ?: similarKlasses.firstOrNull { it.isNativeKt() }
        }
        .toSet()
        //endregion

    private val imports = importedOutermostKlasses
        .filterNot { it.packageName() in AutoImportedPackageNames }
        .map { it.outermostDecl.qualifiedName()!! }
        .sorted()
        .joinToString("\n") { "import $it" }

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
            prefix = "//region Below are simplified types with its built times inside ${srcKlass.noPackageName()!!}.\n/*\n",
            separator = "\n\n",
            postfix = "\n*/\n//endregion",
        )
        //endregion

    // create file
    init{
        Environment.codeGenerator.createFile(
            packageName = GENERATED_PACKAGE,
            fileName = "${srcKlass.contractedName}Elements",
            dependencies = Dependencies(false, srcKlass.containingFile!!),
            content = """
                |$SUPPRESSING
                |
                |package $GENERATED_PACKAGE
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
                .trimMargin()
        )
    }
}