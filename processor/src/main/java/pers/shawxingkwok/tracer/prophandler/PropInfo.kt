package pers.shawxingkwok.tracer.prophandler

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Visibility
import pers.shawxingkwok.ktutil.lazyFast
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getTraceableTypes
import pers.shawxingkwok.tracer.util.isFinal
import pers.shawxingkwok.tracer.shared.getRootNodesPropName
import pers.shawxingkwok.tracer.shared.contractedFakeDotName
import pers.shawxingkwok.tracer.shared.getInterfaceNames

internal sealed class PropInfo(
    val type: Type<*>,
    private val mutable: Boolean,
    v: Visibility,
    val compoundTypeSupported: Boolean,
    private val propsBuilder: PropsBuilder,
){
    private val srcKlass = propsBuilder.srcKlass
    private val srcPropName = getRootNodesPropName(srcKlass)
    private val levelTag = "˚${srcKlass.contractedFakeDotName}"

    // Here needn't consider packageNameTag because it's owned only by other-module
    // declarations.
    private val propInfoNames: List<String> by lazyFast {
        arrayOf(false, true).map { isOuter ->
            buildString {
                // add `` on both sides in case users use special characters themselves.
                append("`_")
                if (isOuter) append("_")

                type.getName(false)
                    .updateIf({ compoundTypeSupported }){
                        it.replace("✕", "")
                    }
                    .let(::append)

                if (!srcKlass.isFinal() || isOuter)
                    append("_$levelTag")

                when(this@PropInfo){
                    is FromSrcKlassSuper -> append("_${srcKlass.contractedFakeDotName}`")
                    is FromElement -> append("_${prop.parentDeclaration!!.contractedFakeDotName}_$prop`")
                }
            }
        }
    }

    private val references: List<String> by lazyFast {
        arrayOf(false, true).map { isOuter ->
            buildString {
                append("`_")
                if (isOuter) append("_")

                when(this@PropInfo) {
                    is FromSrcKlassSuper -> append("$srcPropName`")

                    is FromElement -> {
                        // properties in sourceKlass
                        if (parentProp == null) append(srcPropName)

                        // below are properties in general classes
                        else {
                            append(prop.parentDeclaration!!.contractedFakeDotName)

                            if (!srcKlass.isFinal() || isOuter)
                                append("_$levelTag")

                            append("_${parentProp.parentDeclaration!!.contractedFakeDotName}_$parentProp")
                        }

                        append("`.`$prop`")
                    }
                }
            }
        }
    }

    private val typeContent: String by lazyFast { type.getContent(propsBuilder.imports) }

    private val declContents: List<String> by lazyFast{
        (0..1).map { i ->
            val interfaceName = getInterfaceNames(srcKlass).toList()[i]

            val typePart =
                if (compoundTypeSupported) ""
                else "as $typeContent"

            if (!mutable)
                "${v.name.lowercase()} val $interfaceName.${propInfoNames[i]} inline get() = ${references[i]} $typePart"
            else
                """
                ${v.name.lowercase()} var $interfaceName.${propInfoNames[i]} inline get() = ${references[i]} $typePart    
                    inline set(value){ ${references[i]} = value }
                """.trimIndent()
        }
    }
    val declContent by lazyFast { declContents[0] }
    val outerDeclContent by lazyFast { declContents[1] }

    class FromElement(
        val prop: KSPropertyDeclaration,
        val parentProp:  KSPropertyDeclaration?,
        mutable: Boolean,
        type: Type<*>,
        v: Visibility,
        propsBuilder: PropsBuilder,
    ) :
        PropInfo(
            type = type,
            mutable = mutable,
            v = v,
            compoundTypeSupported = type == prop.getTraceableTypes().first()
                                    && "✕" in type.getName(false),
            propsBuilder = propsBuilder
        )

    class FromSrcKlassSuper(
        val klass: KSClassDeclaration,
        type: Type<*>,
        v: Visibility,
        propsBuilder: PropsBuilder
    ) :
        PropInfo(type, false, v, false, propsBuilder)
}