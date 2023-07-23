package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.tracer.typesystem.Arg
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getBoundProto

private val cache = mutableMapOf<KSClassDeclaration, String>()

public fun getRootNodesPropName(klass: KSClassDeclaration): String =
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