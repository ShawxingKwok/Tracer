package pers.shawxingkwok.tracer.prophandler

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Visibility
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.ktutil.updateIf
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getTraceableTypes
import pers.shawxingkwok.tracer.util.isFinal
import pers.shawxingkwok.tracer.shared.getRootNodesPropName
import pers.shawxingkwok.tracer.shared.contractedFakeDotName
import pers.shawxingkwok.tracer.shared.getInterfaceNames

internal sealed class PropInfo(
    val type: Type<*>,
    private val isMutable: Boolean,
    v: Visibility,
    val compoundTypeSupported: Boolean,
    private val ksPropsBuilder: PropsBuilder,
){
    private val srcKSClass = ksPropsBuilder.srcKSClass
    private val srcPropName = getRootNodesPropName(srcKSClass)
    private val levelTag = "˚${srcKSClass.contractedFakeDotName}"

    // Here needn't consider packageNameTag because it's owned only by other-module
    // declarations.
    private val propInfoNames: List<String> by fastLazy {
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

                if (!srcKSClass.isFinal() || isOuter)
                    append("_$levelTag")

                when(this@PropInfo){
                    is FromSrcKSClassSuper -> append("_${srcKSClass.contractedFakeDotName}`")
                    is FromElement -> append("_${ksProp.parentDeclaration!!.contractedFakeDotName}_$ksProp`")
                }
            }
        }
    }

    private val references: List<String> by fastLazy {
        arrayOf(false, true).map { isOuter ->
            buildString {
                append("`_")
                if (isOuter) append("_")

                when(this@PropInfo) {
                    is FromSrcKSClassSuper -> append("$srcPropName`")

                    is FromElement -> {
                        // properties in sourceKSClass
                        if (parentKSProp == null) append(srcPropName)

                        // below are properties in general classes
                        else {
                            append(ksProp.parentDeclaration!!.contractedFakeDotName)

                            if (!srcKSClass.isFinal() || isOuter)
                                append("_$levelTag")

                            append("_${parentKSProp.parentDeclaration!!.contractedFakeDotName}_$parentKSProp")
                        }

                        append("`.`$ksProp`")
                    }
                }
            }
        }
    }

    private val typeContent: String by fastLazy { type.getContent(ksPropsBuilder.imports) }

    private val declContents: List<String> by fastLazy{
        (0..1).map { i ->
            val interfaceName = getInterfaceNames(srcKSClass).toList()[i]

            val typePart =
                if (compoundTypeSupported) ""
                else "as $typeContent"

            if (!isMutable)
                "${v.name.lowercase()} val $interfaceName.${propInfoNames[i]} inline get() = ${references[i]} $typePart"
            else
                """
                ${v.name.lowercase()} var $interfaceName.${propInfoNames[i]} inline get() = ${references[i]} $typePart    
                    inline set(value){ ${references[i]} = value }
                """.trimIndent()
        }
    }
    val declContent by fastLazy { declContents[0] }
    val outerDeclContent by fastLazy { declContents[1] }

    class FromElement(
        val ksProp: KSPropertyDeclaration,
        val parentKSProp:  KSPropertyDeclaration?,
        isMutable: Boolean,
        type: Type<*>,
        v: Visibility,
        propsBuilder: PropsBuilder,
    ) :
        PropInfo(
            type = type,
            isMutable = isMutable,
            v = v,
            compoundTypeSupported = type == ksProp.getTraceableTypes().first()
                                    && "✕" in type.getName(false),
            ksPropsBuilder = propsBuilder
        )

    class FromSrcKSClassSuper(
        val ksClass: KSClassDeclaration,
        type: Type<*>,
        v: Visibility,
        propsBuilder: PropsBuilder
    ) :
        PropInfo(type, false, v, false, propsBuilder)
}