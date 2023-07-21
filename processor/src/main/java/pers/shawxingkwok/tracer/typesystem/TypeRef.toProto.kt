package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ktutil.lazyFast
import pers.shawxingkwok.tracer.util.isDefNotNull

// This can't be cached with KSType, because differently displayed ksTypes may equal.
internal fun KSTypeReference.toProto(): Type<*> {
    val ksType = resolve()
    val decl = ksType.declaration

    // no typeParameters when decl is KSTypeParameter
    val newArgs = decl.typeParameters
        .zip(this.element?.typeArguments ?: ksType.arguments)
        .map { (param, arg) ->
            val argProtoType by lazyFast { arg.type!!.toProto() }

            when(arg.variance.label){
                "" -> Arg.Simple(argProtoType, param)

                "in" -> when(param.variance.label){
                    "" -> Arg.In(argProtoType, param)
                    "in" -> Arg.Simple(argProtoType, param)
                    else -> error("")
                }

                "out" -> when (param.variance.label) {
                    "" -> Arg.Out(argProtoType, param)
                    "out" -> Arg.Simple(argProtoType, param)
                    else -> error("")
                }

                "*" -> Arg.Star(param)

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
                decl = decl,
                args = newArgs,
                nullable = nullable,
                genericNames = emptyList(),
                hasAlias = true,
                hasConvertibleStar = true,
            )

        is KSTypeAlias ->
            Type.Alias(
                decl = decl,
                args = newArgs,
                nullable = nullable
            )

        else -> error("")
    }
}