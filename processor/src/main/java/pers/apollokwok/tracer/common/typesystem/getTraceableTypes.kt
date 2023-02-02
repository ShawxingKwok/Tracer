package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import pers.apollokwok.ksputil.Log
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.ktutil.updateIf

private val argsMapCache = mutableMapOf<KSClassDeclaration, Map<String, Arg.Out>>().alsoRegister()
private val KSClassDeclaration.argsMap get() =
    if (typeParameters.none())
        emptyMap()
    else
        argsMapCache.getOrPut(this){
            typeParameters.associate { param ->
                val type = param.getBoundProto().convertAll(emptyMap())
                val genericName = param.simpleName()
                val arg = Arg.Out(
                    type = when (type) {
                        is Type.Compound -> type.copy(genericName = genericName, isReturnable = false)
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

private val superTypesCache = Cache.Types<List<Type.Specific>>()

@Suppress("UNCHECKED_CAST")
internal fun KSClassDeclaration.getTraceableSuperTypes(): List<Type.Specific> =
    superTypesCache.getOrPut(asStarProjectedType()){
        if (typeParameters.any())
            getSuperSpecificTypesContainingT().map { it.convertGeneric(argsMap).first }
        else
            getSuperSpecificTypesContainingT()
    }

private val traceableTypesCache = Cache.Types<List<Type<*>>>()

@Suppress("UNCHECKED_CAST")
internal fun KSPropertyDeclaration.getTraceableTypes(): List<Type<*>> {
    val ksType = type.resolve()

    // convert generic, aliases and convertible stars, and get Specific or Compound
    return traceableTypesCache.getOrPut(ksType) {

        val convertedBasicType = run {
            val map = when (val parentDecl = parentDeclaration) {
                is KSClassDeclaration -> parentDecl.argsMap
                null -> emptyMap() // This is only for helping testing top-level properties.
                else -> Log.e("Local properties are forbidden to use.", this)
            }
            type.toProtoWithoutAliasAndStar()
                .updateIf({ map.any() }){
                    it.convertGeneric(map).first
                }
        }

        val convertedSuperTypes = superTypesCache.getOrPut(ksType) {
            fun getSuperTypesOfSpecific(specific: Type.Specific): List<Type.Specific> {
                val mapForConvertingFixedStar = specific
                    .args
                    .filterIsInstance<Arg.General<*>>()
                    .associateBy { it.param.simpleName() }

                val noStarArgs = specific.args
                    .map { arg ->
                        when (arg) {
                            is Arg.General<*> -> arg

                            is Arg.Star -> Arg.Out(
                                type = arg.param.getBoundProto()
                                    .convertAll(mapForConvertingFixedStar)
                                    .updateIf(
                                        { it is Type.Compound },
                                        { (it as Type.Compound).copy(isReturnable = false) }
                                    ),
                                param = arg.param
                            )
                        }
                    }

                val mapForConvertingGeneric = noStarArgs.associateBy { it.param.simpleName() }

                return specific.decl
                    .getSuperSpecificTypesContainingT()
                    .map { it.convertGeneric(mapForConvertingGeneric).first }
            }

            // if the basic type is specific, get its super types
            //      is Compound, get its bound types and flatMap their super types.
            when (convertedBasicType) {
                is Type.Specific -> getSuperTypesOfSpecific(convertedBasicType)

                is Type.Compound -> {
                    val innerTypes = (convertedBasicType.types as List<Type.Specific>)

                    innerTypes + innerTypes.flatMap(::getSuperTypesOfSpecific)
                }

                else -> Bug()
            }
            .map { it.updateNullability(convertedBasicType.isNullable) }
        }

        listOf(convertedBasicType) + convertedSuperTypes
    }
}