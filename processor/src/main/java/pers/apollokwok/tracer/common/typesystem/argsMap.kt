package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.ktutil.Bug

private val cache = mutableMapOf<KSClassDeclaration, Map<String, Arg.Out>>().alsoRegister()

internal val KSClassDeclaration.convertedStarArgsMap get() =
    if (typeParameters.none())
        emptyMap()
    else
        cache.getOrPut(this){
            typeParameters.associate { param ->
                val type = param.getBoundProto().convertAll(emptyMap())
                val genericName = param.simpleName()
                val arg = Arg.Out(
                    type = when (type) {
                        is Type.Compound -> type.copy(genericName = genericName, declarable = false)
                        is Type.Specific -> type.copy(genericName = genericName)
                        else -> Bug()
                    },
                    // this param would be used later, so its conflict with arg type doesn't need
                    // considered.
                    param = param
                )

                param.simpleName() to arg
            }
        }