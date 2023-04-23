package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.tracer.common.typesystem.Arg
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.getBoundProto

private val cache = mutableMapOf<KSClassDeclaration, String>()

public fun getRootNodesName(klass: KSClassDeclaration): String =
    cache.getOrPut(klass) {
        Type.Specific(
            decl = klass,
            args = klass.typeParameters.map { param ->
                Arg.Simple(
                    type = Type.Generic(
                        name = "$param",
                        bound = param.getBoundProto(),
                        nullable = false,
                        isDefNotNull = false
                    ),
                    param = param,
                )
            },
            nullable = false,
            genericNames = emptyList(),
            hasAlias = true,
            hasConvertibleStar = true,
        )
        .convertAll(emptyMap())
        .getName(false)
        .replace("✕", "")
    }