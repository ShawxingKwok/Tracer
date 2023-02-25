package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.apollokwok.ksputil.Log
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.ktutil.updateIf

private val cache = mutableMapOf<KSType, List<Type<*>>>()

@Suppress("UNCHECKED_CAST")
internal fun KSPropertyDeclaration.getTraceableTypes(): List<Type<*>> =
    // convert generic, aliases and convertible stars, and get Specific or Compound
    cache.getOrPut(type.resolve()) {
        val convertedBasicType = run {
            val map = when (val parentDecl = parentDeclaration) {
                is KSClassDeclaration -> parentDecl.convertedStarArgsMap
                null -> emptyMap() // This is only for helping testing top-level properties.
                else -> Log.e("Local properties are forbidden to use.", this)
            }

            type.toProtoWithoutAliasAndStar()
                .updateIf({ map.any() }){
                    it.convertGeneric(map).first
                }
        }

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
                .getSuperSpecificRawTypes(false)
                .map { it.convertGeneric(mapForConvertingGeneric).first }
        }

        // if the basic type is specific, get its super types
        //      is Compound, get its bound types and flatMap their super types.
        val convertedSuperTypes =
            when (convertedBasicType) {
                is Type.Specific -> getSuperTypesOfSpecific(convertedBasicType)

                is Type.Compound -> {
                    val innerTypes = (convertedBasicType.types as List<Type.Specific>)

                    innerTypes + innerTypes.flatMap(::getSuperTypesOfSpecific)
                }

                else -> Bug()
            }
            .map { it.updateNullability(convertedBasicType.isNullable) }

        listOf(convertedBasicType) + convertedSuperTypes
    }