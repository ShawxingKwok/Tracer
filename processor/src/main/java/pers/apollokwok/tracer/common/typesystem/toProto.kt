package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.ktutil.lazyFast

// This can't be cached with KSType, because differently displayed ksTypes may equal.
internal fun KSTypeReference.toProto(): Type<*> {
    val ksType = resolve()
    val decl = ksType.declaration

    // no typeParameters when decl is KSTypeParameter
    val newArgs = decl.typeParameters
        .zip(ksType.arguments)
        .map { (param, arg) ->
            val argProtoType by lazyFast { arg.type!!.toProto() }

            when(arg.variance.label){
                "" -> Arg.Simple(argProtoType, param)

                "in" -> when(param.variance.label){
                    "" -> Arg.In(argProtoType, param)
                    "in" -> Arg.Simple(argProtoType, param)
                    else -> Bug()
                }

                "out" -> when (param.variance.label) {
                    "" -> Arg.Out(argProtoType, param)
                    "out" -> Arg.Simple(argProtoType, param)
                    else -> Bug()
                }

                "*" -> Arg.Star(param)

                else -> Bug()
            }
        }

    val isNullable = ksType.isMarkedNullable

    return when(decl) {
        is KSTypeParameter ->
            Type.Generic(
                name = "$decl",
                isNullable = isNullable,
                bound = decl.getBoundProto(),
            )

        is KSClassDeclaration ->
            Type.Specific(
                decl = decl,
                args = newArgs,
                isNullable = isNullable,
                genericName = null,
                hasAlias = true,
                hasConvertibleStar = true
            )

        is KSTypeAlias ->
            Type.Alias(
                decl = decl,
                args = newArgs,
                isNullable = isNullable
            )

        else -> Bug()
    }
}