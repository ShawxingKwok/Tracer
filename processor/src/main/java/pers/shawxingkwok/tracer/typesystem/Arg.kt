package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import pers.shawxingkwok.ksputil.Imports
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.ktutil.updateIf

internal sealed class Arg<T: Arg<T>>(val param: KSTypeParameter) : Convertible<Arg<*>>(){
    sealed class General<T: General<T>>(
        val type: Type<*>,
        ksParam: KSTypeParameter
    ) :
        Arg<General<*>>(ksParam)
    {
        final override fun convertAlias(): T = copy(type.convertAlias())
        final override fun convertStar(): T = copy(type.convertStar())

        // this is the core part
        final override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean,
        ): Pair<Arg<*>, Boolean> {
            val selfActualVarianceLabel =
                when (this) {
                    is In -> "in"
                    is Out -> "out"
                    is Simple -> param.variance.label
                }

            return if (type is Type.Generic) {
                val mappedArg = map[type.name]
                val convertedType by fastLazy { type.convertGeneric(map, isFromAlias).first }
                val requireOut = !isFromAlias && mappedArg !is Simple

                val newArg = when (mappedArg) {
                    // This condition would be removed when typealias bounds are officially required.
                    is Star -> {
                        require(isFromAlias)
                        Star(param)
                    }

                    is Out, null -> when (selfActualVarianceLabel) {
                        "" -> Out(convertedType, param)
                        "in" -> Star(param)
                        "out" -> copy(convertedType)
                        else -> error("")
                    }

                    is In -> when (selfActualVarianceLabel) {
                        "" -> In(convertedType, param)
                        "in" -> copy(convertedType)
                        // change to require bound, but with a new map without current substitute arg.
                        "out" -> convertGeneric(map - type.name, isFromAlias).first
                        else -> error("")
                    }

                    is Simple -> copy(convertedType)
                }

                newArg to requireOut
            } else {
                val (convertedType, requireOut) = type.convertGeneric(map, isFromAlias)
                val newArg = when {
                    !requireOut -> copy(type = convertedType)
                    selfActualVarianceLabel == "" -> Out(type = convertedType, param)
                    selfActualVarianceLabel == "in" -> Star(param)
                    selfActualVarianceLabel == "out" -> copy(type = convertedType)
                    else -> error("")
                }
                newArg to requireOut
            }
        }

        //region fun copy
        @Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
        fun copy(
            type: Type<*> = this.type,
            ksParam: KSTypeParameter = this.param,
        ): T =
            when{
                type == this.type && ksParam == this.param -> this
                this is Simple -> Simple(type, ksParam)
                this is In -> In(type, ksParam)
                this is Out -> Out(type, ksParam)
                else -> error("")
            } as T
        //endregion

        final override fun hashCode(): Int {
            return 31 * type.hashCode() + param.hashCode()
        }

        final override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (this.javaClass != other?.javaClass) return false

            other as General<*>

            if (type != other.type) return false
            return param == other.param
        }

        final override val allInnerKSClasses: List<KSClassDeclaration> by fastLazy { type.allInnerKSClasses }
    }

    class Simple(type: Type<*>, ksParam: KSTypeParameter) : General<Simple>(type, ksParam) {
        override fun toString(): String = "$type"

        override fun getContent(imports: Imports): String =
            type.getContent(imports)

        override fun getName(isGross: Boolean): String =
            type.getName(isGross)
    }

    class In(type: Type<*>, ksParam: KSTypeParameter) : General<In>(type, ksParam) {
        override fun toString(): String = "in $type"

        override fun getContent(imports: Imports): String =
            type.getContent(imports)
                .updateIf({ type !is Type.Compound }){
                    "in $it"
                }

        override fun getName(isGross: Boolean): String =
            buildString {
                if (!isGross) append("↑")
                append(type.getName(isGross))
            }
    }

    class Out(type: Type<*>, ksParam: KSTypeParameter) : General<Out>(type, ksParam) {
        override fun toString(): String = "out $type"

        override fun getContent(imports: Imports): String =
            type.getContent(imports)
                .updateIf({ type !is Type.Compound }){
                    "out $it"
                }

        override fun getName(isGross: Boolean): String =
            buildString {
                if (!isGross) append("↓")
                append(type.getName(isGross))
            }
    }

    // bound is for being passed to super types if it's in the first level
    class Star(ksParam: KSTypeParameter) : Arg<Star>(ksParam) {
        //region conversion
        // later
        override fun convertAlias(): Star = this

        @Deprecated(
            message = "",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("convertStar(map)")
        )
        override fun convertStar(): Arg<*> = error("")

        // later
        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean
        ):
            Pair<Arg<*>, Boolean> = this to false

        override fun toString(): String = "*"

        fun convertStar(map: Map<String, General<*>>): Arg<*> =
            when {
                param.variance.label == "in"
                // check recycle like Enum<*>
                || param.parentDeclaration!! == param.bounds.first() -> this

                param.variance.label == ""
                    -> Out(param.getBoundProto(), param).convertAll(map)

                param.variance.label == "out"
                    -> Simple(param.getBoundProto(), param).convertAll(map)

                else -> error("")
            }
        //endregion

        //region fixed part
        override val allInnerKSClasses: List<KSClassDeclaration> = emptyList()
        override fun getContent(imports: Imports): String = "*"
        override fun getName(isGross: Boolean): String = "✶"
        //endregion

        override fun hashCode(): Int {
            return 31 * javaClass.hashCode() + param.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Star) return false
            return param == other.param
        }
    }
}