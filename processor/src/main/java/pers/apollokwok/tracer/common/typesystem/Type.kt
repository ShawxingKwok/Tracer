package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import pers.apollokwok.ksputil.*
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.ktutil.lazyFast
import pers.apollokwok.tracer.common.shared.contractedFakeDotName

internal sealed class Type<T: Type<T>>(val nullable: Boolean) : Convertible<Type<*>>(){
    companion object{
        // use 'get()=' since anyType.declaration varies every round.
        @Suppress("DANGEROUS_CHARACTERS", "ObjectPropertyName", "NonAsciiCharacters")
        val `Any？` get() = Specific(
            decl = resolver.builtIns.anyType.declaration as KSClassDeclaration,
            args = emptyList(),
            nullable = true,
            hasAlias = false,
            hasConvertibleStar = false,
        )
    }

    final override fun toString(): String = getName(false)

    class Generic(
        val name: String,
        val bound: Type<*>,
        nullable: Boolean,
        val isDefNotNull: Boolean,
    ) :
        Type<Generic>(nullable)
    {
        //region conversion
        override fun convertAlias(): Generic = this

        override fun convertStar(): Generic = this

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            fromAlias: Boolean,
        ): Pair<Type<*>, Boolean> {
            val arg = map[name]

            val requireOut = !fromAlias && arg !is Arg.Simple

            val type = when(arg){
                null -> {
                    require(!fromAlias)
                    when (val it = bound.convertAll(map)) {
                        is Generic, is Alias -> Bug()
                        is Compound -> it.copy(genericNames = listOf(name) + it.genericNames)
                        is Specific -> it.copy(genericNames = listOf(name) + it.genericNames)
                    }
                }

                is Arg.Star -> error("This should be handled by parent arg.")

                is Arg.General<*> -> arg.type
            }

            val nullable = this.nullable || (!isDefNotNull && type.nullable)

            val newType = type.updateNullability(nullable)

            return newType to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKlasses: List<KSClassDeclaration> get() = Bug()

        override fun getContent(imports: Imports): String =
            buildString{
                append(name)
                when{
                    nullable -> append("?")
                    isDefNotNull -> append(" & Any")
                }
            }

        override fun getName(isGross: Boolean): String =
            buildString{
                append(name)
                when{
                    nullable -> append("?")
                    isDefNotNull -> append(" & Any")
                }
            }
        //endregion

        //region common
        fun copy(
            name: String = this.name,
            bound: Type<*> = this.bound,
            nullable: Boolean = this.nullable,
            isDefNotNull: Boolean = this.isDefNotNull,
        ) =
            if (name == this.name
                && bound == this.bound
                && nullable == this.nullable
                && isDefNotNull == this.isDefNotNull
            )
                this
            else
                Generic(name, bound, nullable, isDefNotNull)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Generic

            if (name != other.name) return false
            if (bound != other.bound) return false
            if (nullable != other.nullable) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + bound.hashCode()
            result = 31 * result + nullable.hashCode()
            return result
        }
        //endregion
    }

    class Alias(
        val decl: KSTypeAlias,
        val args: List<Arg<*>>,
        nullable: Boolean,
    ) :
        Type<Alias>(nullable)
    {
        //region conversion
        override fun convertAlias(): Specific {
            val newMap = args.map { it.convertAlias() }.associateBy { it.param.simpleName() }

            val converted = decl
                .type
                .toProto()
                .convertGeneric(newMap, true)
                .first
                .convertAlias() as Specific

            val nullable = this.nullable || converted.nullable

            return converted.copy(hasAlias = false, nullable = nullable)
        }

        // may be unreachable
        override fun convertStar(): Alias = error("There should be no alias when converting star.")

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            fromAlias: Boolean,
        ): Pair<Alias, Boolean> {
            val replaced = args.map { it.convertGeneric(map, fromAlias) }
            val requireOut = replaced.any { it.second }
            return copy(args = replaced.map { it.first }) to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKlasses: List<KSClassDeclaration> get() = Bug()

        override fun getContent(imports: Imports): String = Bug()

        override fun getName(isGross: Boolean): String =
            buildString {
                append(decl)
                if (args.any()) {
                    append("‹")
                    append(args.joinToString { "，" })
                    append("›")
                }
                if (nullable) append("？")
            }
        //endregion

        //region common
        fun copy(
            decl: KSTypeAlias = this.decl,
            args: List<Arg<*>> = this.args,
            nullable: Boolean = this.nullable,
        ) =
            if (decl == this.decl
                && args == this.args
                && nullable == this.nullable
            )
                this
            else
                Alias(decl, args, nullable)

        override fun hashCode(): Int {
            var result = decl.hashCode()
            result = 31 * result + args.hashCode()
            result = 31 * result + nullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Alias

            if (decl != other.decl) return false
            if (args != other.args) return false
            if (nullable != other.nullable) return false

            return true
        }
        //endregion
    }

    class Specific(
        val decl: KSClassDeclaration,
        val args: List<Arg<*>>,
        nullable: Boolean,
        val genericNames: List<String> = emptyList(),
        val hasAlias: Boolean,
        val hasConvertibleStar: Boolean,
    ) :
        Type<Specific>(nullable)
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
            fromAlias: Boolean
        ): Pair<Specific, Boolean> {
            val replaced = args.map { it.convertGeneric(map, fromAlias) }
            val requireOut = replaced.any { it.second }
            return copy(args = replaced.map { it.first }) to requireOut
        }
        //endregion

        //region fixed part
        override val allInnerKlasses: List<KSClassDeclaration> by lazyFast {
            args.flatMap { it.allInnerKlasses } + decl
        }

        override fun getContent(imports: Imports): String =
            buildString{
                append(imports.getName(decl))

                if (args.any()){
                    append("<")

                    args.joinToString(", ") {
                        it.getContent(imports)
                    }
                    .let(::append)

                    append(">")
                }

                if (nullable) append("?")
            }

        // gross and the other
        override fun getName(isGross: Boolean): String =
            buildString {
                @Suppress("LocalVariableName", "NonAsciiCharacters")
                val `need？` = nullable && !isGross

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
                        decl.qualifiedName()!!.startsWith(it)
                        && decl.qualifiedName()!!.substringAfter(it).all(Char::isDigit)
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
                    append(decl.contractedFakeDotName)

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
            decl: KSClassDeclaration = this.decl,
            args: List<Arg<*>> = this.args,
            nullable: Boolean = this.nullable,
            genericNames: List<String> = this.genericNames,
            hasAlias: Boolean = this.hasAlias,
            hasConvertibleStar: Boolean = this.hasConvertibleStar,
        ): Specific =
            if (decl == this.decl
                && args == this.args
                && nullable == this.nullable
                && genericNames == this.genericNames
                && hasAlias == this.hasAlias
                && hasConvertibleStar == this.hasConvertibleStar
            )
                this
            else
                Specific(decl, args, nullable, genericNames, hasAlias, hasConvertibleStar)

        override fun hashCode(): Int {
            var result = decl.hashCode()
            result = 31 * result + args.hashCode()
            result = 31 * result + genericNames.hashCode()
            result = 31 * result + hasAlias.hashCode()
            result = 31 * result + hasConvertibleStar.hashCode()
            result = 31 * result + nullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Specific

            if (decl != other.decl) return false
            if (args != other.args) return false
            if (genericNames != other.genericNames) return false
            if (hasAlias != other.hasAlias) return false
            if (hasConvertibleStar != other.hasConvertibleStar) return false
            if (nullable != other.nullable) return false

            return true
        }
        //endregion
    }

    class Compound(
        val types: List<Type<*>>,
        nullable: Boolean,
        val genericNames: List<String> = emptyList(),
    ):
        Type<Compound>(nullable)
    {
        init {
            require(types.count() >= 2)
        }

        //region conversion
        override fun convertAlias(): Compound = copy(types = types.map { it.convertAlias() })

        override fun convertStar(): Compound = copy(types = types.map { it.convertStar() })

        override fun convertGeneric(
            map: Map<String, Arg<*>>,
            fromAlias: Boolean
        ):
            Pair<Compound, Boolean> = this to false
        //endregion

        //region fixed part
        override val allInnerKlasses: List<KSClassDeclaration> = emptyList()

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
                    if (nullable) append("？")
                    if (genericNames.any()) append("✕")
                }
            }
        //endregion

        //region copy
        fun copy(
            types: List<Type<*>> = this.types,
            nullable: Boolean = this.nullable,
            genericNames: List<String> = this.genericNames,
        ): Compound =
            if (types == this.types
                && nullable == this.nullable
                && genericNames == this.genericNames
            )
                this
            else
                Compound(types, nullable, genericNames)

        override fun hashCode(): Int {
            var result = types.hashCode()
            result = 31 * result + genericNames.hashCode()
            result = 31 * result + nullable.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Compound

            if (types != other.types) return false
            if (genericNames != other.genericNames) return false
            if (nullable != other.nullable) return false

            return true
        }
        //endregion
    }
}