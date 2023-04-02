package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.ktutil.updateIf

internal fun getSrcKlassTraceableSuperTypes(srcKlass: KSClassDeclaration): List<Type.Specific> =
    srcKlass.getSuperSpecificRawTypes(true)
        .updateIf({ srcKlass.typeParameters.any() }){ rawTypes ->
            val map = srcKlass.typeParameters.associate { param ->
                val type = param.getBoundProto().convertAll(emptyMap())
                val genericName = param.simpleName()
                val arg = Arg.Out(
                    type = when (type) {
                        is Type.Compound -> type.copy(genericNames = listOf(genericName))
                        is Type.Specific -> type.copy(genericNames = listOf(genericName))
                        else -> Bug()
                    },
                    // this param would be used later, so its conflict with arg type doesn't need
                    // considered.
                    param = param
                )

                param.simpleName() to arg
            }

            rawTypes.map { it.convertGeneric(map).first }
        }