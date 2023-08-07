package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.tracer.typesystem.Arg
import pers.shawxingkwok.tracer.typesystem.Type
import pers.shawxingkwok.tracer.typesystem.getBoundProto

private val cache = mutableMapOf<KSClassDeclaration, String>()

public fun getRootNodesPropName(ksClass: KSClassDeclaration): String =
    cache.getOrPut(ksClass) {
        Type.Specific(
            ksClass = ksClass,
            args = ksClass.typeParameters.map { param ->
                Arg.Simple(
                    type = Type.Generic(
                        name = "$param",
                        bound = param.getBoundProto(),
                        nullable = false,
                        isDefNotNull = false
                    ),
                    ksParam = param,
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