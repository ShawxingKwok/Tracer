package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.ktutil.updateIf

internal fun getSrcKSClassTraceableSuperTypes(srcKSClass: KSClassDeclaration): List<Type.Specific> =
    srcKSClass.getSuperSpecificRawTypes(true)
        .updateIf({ srcKSClass.typeParameters.any() }){ rawTypes ->
            val map = srcKSClass.typeParameters.associate { param ->
                val type = param.getBoundProto().convertAll(emptyMap())
                val genericName = param.simpleName()
                val arg = Arg.Out(
                    type = when (type) {
                        is Type.Compound -> type.copy(genericNames = listOf(genericName))
                        is Type.Specific -> type.copy(genericNames = listOf(genericName))
                        else -> error("")
                    },
                    // this param would be used later, so its conflict with arg type doesn't need
                    // considered.
                    ksParam = param
                )

                param.simpleName() to arg
            }

            rawTypes.map { it.convertGeneric(map).first }
        }