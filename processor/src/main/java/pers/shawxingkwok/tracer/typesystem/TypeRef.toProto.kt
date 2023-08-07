package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ktutil.fastLazy
import pers.shawxingkwok.tracer.util.isDefNotNull

// This can't be cached with KSType, because differently displayed ksTypes may equal.
internal fun KSTypeReference.toProto(): Type<*> {
    val ksType = resolve()
    val decl = ksType.declaration

    // no typeParameters when decl is KSTypeParameter
    val newArgs = decl.typeParameters
        .zip(this.element?.typeArguments ?: ksType.arguments)
        .map { (ksParam, ksArg) ->
            val argProtoType by fastLazy { ksArg.type!!.toProto() }

            when(ksArg.variance.label){
                "" -> Arg.Simple(argProtoType, ksParam)

                "in" -> when(ksParam.variance.label){
                    "" -> Arg.In(argProtoType, ksParam)
                    "in" -> Arg.Simple(argProtoType, ksParam)
                    else -> error("")
                }

                "out" -> when (ksParam.variance.label) {
                    "" -> Arg.Out(argProtoType, ksParam)
                    "out" -> Arg.Simple(argProtoType, ksParam)
                    else -> error("")
                }

                "*" -> Arg.Star(ksParam)

                else -> error("")
            }
        }

    val nullable = ksType.isMarkedNullable

    return when(decl) {
        is KSTypeParameter ->
            Type.Generic(
                name = "$decl",
                nullable = nullable,
                bound = decl.getBoundProto(),
                isDefNotNull = this.isDefNotNull()
            )

        is KSClassDeclaration ->
            Type.Specific(
                ksClass = decl,
                args = newArgs,
                nullable = nullable,
                genericNames = emptyList(),
                hasAlias = true,
                hasConvertibleStar = true,
            )

        is KSTypeAlias ->
            Type.Alias(
                ksTypeAlias = decl,
                args = newArgs,
                nullable = nullable
            )

        else -> error("")
    }
}