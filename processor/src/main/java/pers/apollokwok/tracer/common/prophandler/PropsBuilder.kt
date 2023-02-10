package pers.apollokwok.tracer.common.prophandler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.prophandler.PropInfo.Companion.process
import pers.apollokwok.tracer.common.shared.*
import pers.apollokwok.tracer.common.shared.Names.GENERATED_PACKAGE
import pers.apollokwok.tracer.common.shared.Tags.AllInternal
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.getTraceableSuperTypes
import pers.apollokwok.tracer.common.typesystem.getTraceableTypes
import pers.apollokwok.tracer.common.util.*

internal class PropsBuilder(val sourceKlass: KSClassDeclaration) {
    private val record = object {
        val validlyTracedInsideKlasses: Set<KSClassDeclaration> = mutableSetOf()
        val tracedKlassesStoppedTracingInsideForNullability: Set<KSClassDeclaration> = mutableSetOf()

        fun getErrorMsg(klass: KSClassDeclaration) =
            "Annotate $klass with ${Names.Nodes} or ${Names.Tips} since it appeared in " +
                "$sourceKlass for multiple times and it is in the current module with some " +
                "not omitted visible properties to trace."
    }

    private val newPropsInfo = mutableListOf<PropInfo>()

    private fun getV(decl: KSDeclaration, type: Type<*>): Visibility?{
        val limitedV = limitVisibility(decl, *type.allInnerKlasses.toTypedArray())

        return when {
            limitedV == null -> null
            AllInternal -> Visibility.INTERNAL
            else -> limitedV
        }
    }

    private fun trace(klass: KSClassDeclaration, parentProp: KSPropertyDeclaration?){
        klass.getPreNeededProperties()
            .asSequence()
            // Omit Any and warn
            .filterNot { prop->
                val isAny = prop.type.resolve().declaration == Type.`Any？`.decl

                if (isAny)
                    Log.w(
                        msg = "This property would be omitted because its type is Any. " +
                            "You can annotate this property with ${Names.Declare}(false) " +
                            "to make it look clear.",
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

                    val isMutable = prop.isMutable
                        && i == 0
                        && !(klass == sourceKlass
                            && sourceKlass.typeParameters.any()
                            && kotlin.run {
                                fun KSTypeReference.containT(): Boolean =
                                    resolve().declaration is KSTypeParameter
                                    || element?.typeArguments?.any { it.type?.containT() == true } == true

                                prop.type.containT()
                            })

                    newPropsInfo += PropInfo.FromElement(prop, parentProp, isMutable, type, v, this)
                }
            }
            // filter and trace inside, other filtering conditions are in 'getPreNeededProperties'
            .mapNotNull { (prop, types)->
                val basicType = types.first() as? Type.Specific ?: return@mapNotNull null
                prop to basicType
            }
            .filterNot { (_, basicType)-> basicType.decl.isAnnotatedRootOrNodes() }
            .filterNot { (prop, basicType)->
                if (basicType.isNullable) {
                    Log.require(
                        condition = basicType.decl !in record.validlyTracedInsideKlasses,
                        symbols = listOf(prop, klass),
                    ){
                        record.getErrorMsg(basicType.decl)
                    }
                    (record.tracedKlassesStoppedTracingInsideForNullability as MutableSet) += basicType.decl
                }

                basicType.isNullable
            }
            .forEach { (prop, basicType)->
                trace(
                    klass = basicType.decl,
                    parentProp = prop
                )
            }
    }

    // collect sourceKlass superTypes in new props
    init {
        sourceKlass.getTraceableSuperTypes().forEach { type ->
            val v = getV(sourceKlass, type) ?: return@forEach
            newPropsInfo += PropInfo.FromSrcKlassSuper(sourceKlass, type, false, v, this)
        }
    }

    // start tracing inner property types in recurse in new props
    init {
        trace(sourceKlass, null)
    }

    private val allInnerKlasses = newPropsInfo.flatMap { it.type.allInnerKlasses }.toSet()

    val importedOutermostKlasses = allInnerKlasses
        //region
        .map { it.outermostDecl }
        .groupBy { it.simpleName() }
        .mapNotNull { (_, similarKlasses)->
            similarKlasses.singleOrNull() ?: similarKlasses.firstOrNull { it.isNative() }
        }
        .toSet()
        //endregion

    // for these three classes in a same file,
    // 'java.io.File' and 'com.lib.File' from library, and native 'com.example.File',
    // srcTags should be java, com¸lib
    val srcTags: Map<KSClassDeclaration, String> = run {
        // clear though time-consuming, and the result is mostly empty
        var i = 0
        val ret = mutableMapOf<KSClassDeclaration, String>()

        var similarKlasses = allInnerKlasses.filterOutRepeated { it.contractedName }.toSet()

        while (similarKlasses.any()){
            i++

            val grouped = similarKlasses.groupBy{
                it.packageName().split(".").take(i).joinToString("¸")
            }

            ret += grouped.filter { it.value.size == 1 }
                .map { (srcTag, klasses)-> klasses.first() to srcTag }

            similarKlasses = grouped.filter { it.value.size > 1 }.values.flatten().toSet()
        }

        ret.filterKeys { !it.isNative() }
    }

    // process new props, make some declared with its owner name or prop name further.
    init {
        newPropsInfo.process()
    }

    private val imports = importedOutermostKlasses
        .filterNot { it.packageName() in AutoImportedPackageNames }
        .map { it.outermostDecl.qualifiedName()!! }
        .sorted()
        .joinToString("\n") { "import $it" }

    private val builtTimesComment: String =
        //region
        newPropsInfo
        .groupingBy { it.grossKey }
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
            prefix = "//region Below are simplified types with its built times inside ${sourceKlass.noPackageName()!!}.\n/*\n",
            separator = "\n\n",
            postfix = "\n*/\n//endregion",
        )
        //endregion

    // create file
    init{
        Environment.codeGenerator.createFile(
            packageName = GENERATED_PACKAGE,
            fileName = "${sourceKlass.contractedName}InnerElements",
            dependencies = Dependencies(false, sourceKlass.containingFile!!),
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