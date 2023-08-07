package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import pers.shawxingkwok.ksputil.Imports
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.tracer.shared.contractedFakeDotName

internal sealed class Type<T: Type<T>>(val isNullable: Boolean) : Convertible<Type<*>>(){
    companion object{
        // use 'get()=' since anyType.declaration varies every round.
        @Suppress("ObjectPropertyName", "NonAsciiCharacters")
        val `Any？` get() = Specific(
            ksClass = resolver.builtIns.anyType.declaration as KSClassDeclaration,
            args = emptyList(),
            isNullable = true,
            hasAlias = false,
            hasConvertibleStar = false,
        )
    }

    final override fun toString(): String = getName(false)

    class Generic(
        val name: String,
        val bound: Type<*>,
        isNullable: Boolean,
        val isDefNotNull: Boolean,
    ) :
        Type<Generic>(isNullable)
    {
        //region conversion
        override fun convertAlias(): Generic = this

        override fun convertStar(): Generic = this

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean,
        ): Pair<Type<*>, Boolean> {
            val arg = map[name]

            val requireOut = !isFromAlias && arg !is Arg.Simple

            val type = when(arg){
                null -> {
                    require(!isFromAlias)
                    when (val it = bound.convertAll(map)) {
                        is Generic, is Alias -> error("")
                        is Compound -> it.copy(genericNames = listOf(name) + it.genericNames)
                        is Specific -> it.copy(genericNames = listOf(name) + it.genericNames)
                    }
                }

                is Arg.Star -> error("This should be handled by parent arg.")

                is Arg.General<*> -> arg.type
            }

            val isNullable = this.isNullable || (!isDefNotNull && type.isNullable)

            val newType = type.updateNullability(isNullable)

            return newType to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKSClasses: List<KSClassDeclaration> get() = error("")

        override fun getContent(imports: Imports): String =
            buildString{
                append(name)
                when{
                    isNullable -> append("?")
                    isDefNotNull -> append(" & Any")
                }
            }

        override fun getName(isGross: Boolean): String =
            buildString{
                append(name)
                when{
                    isNullable -> append("?")
                    isDefNotNull -> append(" & Any")
                }
            }
        //endregion

        //region common
        fun copy(
            name: String = this.name,
            bound: Type<*> = this.bound,
            isNullable: Boolean = this.isNullable,
            isDefNotNull: Boolean = this.isDefNotNull,
        ) =
            if (name == this.name
                && bound == this.bound
                && isNullable == this.isNullable
                && isDefNotNull == this.isDefNotNull
            )
                this
            else
                Generic(name, bound, isNullable, isDefNotNull)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Generic

            if (name != other.name) return false
            if (bound != other.bound) return false
            return isNullable == other.isNullable
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + bound.hashCode()
            result = 31 * result + isNullable.hashCode()
            return result
        }
        //endregion
    }

    class Alias(
        val ksTypeAlias: KSTypeAlias,
        val args: List<Arg<*>>,
        isNullable: Boolean,
    ) :
        Type<Alias>(isNullable)
    {
        //region conversion
        override fun convertAlias(): Specific {
            val newMap = args.map { it.convertAlias() }.associateBy { it.param.simpleName() }

            val converted = ksTypeAlias
                .type
                .toProto()
                .convertGeneric(newMap, true)
                .first
                .convertAlias() as Specific

            val isNullable = this.isNullable || converted.isNullable

            return converted.copy(hasAlias = false, isNullable = isNullable)
        }

        // may be unreachable
        override fun convertStar(): Alias = error("There should be no alias when converting star.")

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean,
        ): Pair<Alias, Boolean> {
            val replaced = args.map { it.convertGeneric(map, isFromAlias) }
            val requireOut = replaced.any { it.second }
            return copy(args = replaced.map { it.first }) to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKSClasses: List<KSClassDeclaration> get() = error("")

        override fun getContent(imports: Imports): String = error("")

        override fun getName(isGross: Boolean): String =
            buildString {
                append(ksTypeAlias)
                if (args.any()) {
                    append("‹")
                    append(args.joinToString { "，" })
                    append("›")
                }
                if (isNullable) append("？")
            }
        //endregion

        //region common
        fun copy(
            ksTypeAlias: KSTypeAlias = this.ksTypeAlias,
            args: List<Arg<*>> = this.args,
            isNullable: Boolean = this.isNullable,
        ) =
            if (ksTypeAlias == this.ksTypeAlias
                && args == this.args
                && isNullable == this.isNullable
            )
                this
            else
                Alias(ksTypeAlias, args, isNullable)

        override fun hashCode(): Int {
            var result = ksTypeAlias.hashCode()
            result = 31 * result + args.hashCode()
            result = 31 * result + isNullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Alias

            if (ksTypeAlias != other.ksTypeAlias) return false
            if (args != other.args) return false
            return isNullable == other.isNullable
        }
        //endregion
    }

    class Specific(
        val ksClass: KSClassDeclaration,
        val args: List<Arg<*>>,
        isNullable: Boolean,
        val genericNames: List<String> = emptyList(),
        val hasAlias: Boolean,
        val hasConvertibleStar: Boolean,
    ) :
        Type<Specific>(isNullable)
    {
        //region conversion
        override fun convertAlias(): Specific =
            if (hasAlias)
                copy(args = args.map { it.convertAlias() })
            else
                this

        override fun convertStar(): Specific {
            if (!hasConvertibleStar) return this

            val fromGeneral = args
                .filterIsInstance<Arg.General<*>>()
                .map { it.convertStar() }

            val map = fromGeneral.associateBy { it.param.simpleName() }

            val fromStar = args
                .filterIsInstance<Arg.Star>()
                .map { it.convertStar(map) }

            val convertedArgs = fromGeneral + fromStar

            val newArgs= args.map { arg ->
                convertedArgs.first { it.param == arg.param }
            }

            return copy(args = newArgs, hasConvertibleStar = false)
        }

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean
        ): Pair<Specific, Boolean> {
            val replaced = args.map { it.convertGeneric(map, isFromAlias) }
            val requireOut = replaced.any { it.second }
            return copy(args = replaced.map { it.first }) to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKSClasses: List<KSClassDeclaration> by fastLazy {
            args.flatMap { it.allInnerKSClasses } + ksClass
        }

        override fun getContent(imports: Imports): String =
            buildString{
                append(imports.getKSClassName(ksClass))

                if (args.any()){
                    append("<")

                    args.joinToString(", ") {
                        it.getContent(imports)
                    }
                    .let(::append)

                    append(">")
                }

                if (isNullable) append("?")
            }

        // gross and the other
        override fun getName(isGross: Boolean): String =
            buildString {
                @Suppress("LocalVariableName", "NonAsciiCharacters")
                val `need？` = isNullable && !isGross

                if (!isGross)
                    genericNames.forEach {
                        append("$it-")
                    }

                val (isGeneralFunction, isSuspendFunction) =
                    arrayOf(
                        Function::class.qualifiedName!!,
                        "kotlin.coroutines.SuspendFunction",
                    )
                    .map {
                        ksClass.qualifiedName()!!.startsWith(it)
                        && ksClass.qualifiedName()!!.substringAfter(it).all(Char::isDigit)
                    }

                if (isGeneralFunction || isSuspendFunction) {
                    val body = buildString {
                        if (isSuspendFunction && !isGross) append("⍒")
                        append("❨")

                        args.dropLast(1)
                            .joinToString("，") { it.getName(isGross) }
                            .let(::append)

                        append("❩→")
                        append(args.last().getName(isGross))
                    }

                    if (`need？`)
                        append("❨$body❩？")
                    else
                        append(body)
                } else {
                    append(ksClass.contractedFakeDotName)

                    if (`need？` && args.none()) append("？")

                    if (args.any()) {
                        args.joinToString(
                            prefix = "‹",
                            transform = { it.getName(isGross) },
                            separator = "，",
                            postfix = "›",
                        )
                        .let(::append)

                        if (`need？`) append("？")
                    }
                }
            }
        //endregion

        //region common
        fun copy(
            ksClass: KSClassDeclaration = this.ksClass,
            args: List<Arg<*>> = this.args,
            isNullable: Boolean = this.isNullable,
            genericNames: List<String> = this.genericNames,
            hasAlias: Boolean = this.hasAlias,
            hasConvertibleStar: Boolean = this.hasConvertibleStar,
        ): Specific =
            if (ksClass == this.ksClass
                && args == this.args
                && isNullable == this.isNullable
                && genericNames == this.genericNames
                && hasAlias == this.hasAlias
                && hasConvertibleStar == this.hasConvertibleStar
            )
                this
            else
                Specific(ksClass, args, isNullable, genericNames, hasAlias, hasConvertibleStar)

        override fun hashCode(): Int {
            var result = ksClass.hashCode()
            result = 31 * result + args.hashCode()
            result = 31 * result + genericNames.hashCode()
            result = 31 * result + hasAlias.hashCode()
            result = 31 * result + hasConvertibleStar.hashCode()
            result = 31 * result + isNullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Specific

            if (ksClass != other.ksClass) return false
            if (args != other.args) return false
            if (genericNames != other.genericNames) return false
            if (hasAlias != other.hasAlias) return false
            if (hasConvertibleStar != other.hasConvertibleStar) return false
            return isNullable == other.isNullable
        }
        //endregion
    }

    class Compound(
        val types: List<Type<*>>,
        isNullable: Boolean,
        val genericNames: List<String> = emptyList(),
    ):
        Type<Compound>(isNullable)
    {
        init {
            require(types.count() >= 2)
        }

        //region conversion
        override fun convertAlias(): Compound = copy(types = types.map { it.convertAlias() })

        override fun convertStar(): Compound = copy(types = types.map { it.convertStar() })

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            isFromAlias: Boolean
        ):
            Pair<Compound, Boolean> = this to false
        //endregion

        //region fixed part
        override val allInnerKSClasses: List<KSClassDeclaration> = emptyList()

        override fun getContent(imports: Imports): String = "*"

        override fun getName(isGross: Boolean): String =
            buildString {
                genericNames.forEach {
                    append("$it-")
                }
                append("‹")
                append(types.joinToString("，"){ it.getName(isGross) })
                append("›")
                if (!isGross){
                    if (isNullable) append("？")
                    if (genericNames.any()) append("✕")
                }
            }
        //endregion

        //region copy
        fun copy(
            types: List<Type<*>> = this.types,
            isNullable: Boolean = this.isNullable,
            genericNames: List<String> = this.genericNames,
        ): Compound =
            if (types == this.types
                && isNullable == this.isNullable
                && genericNames == this.genericNames
            )
                this
            else
                Compound(types, isNullable, genericNames)

        override fun hashCode(): Int {
            var result = types.hashCode()
            result = 31 * result + genericNames.hashCode()
            result = 31 * result + isNullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Compound

            if (types != other.types) return false
            if (genericNames != other.genericNames) return false
            return isNullable == other.isNullable
        }
        //endregion
    }
}