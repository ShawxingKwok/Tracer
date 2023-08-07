package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.simpleName

private val cache = mutableMapOf<KSType, List<Type<*>>>()

@Suppress("UNCHECKED_CAST")
internal fun KSPropertyDeclaration.getTraceableTypes(): List<Type<*>> =
    // convert generic, aliases and convertible stars, and get Specific or Compound
    cache.getOrPut(type.resolve()) {
        val convertedBasicType = type.toProto().convertAll(emptyMap())

        fun getSuperTypesOfSpecific(specific: Type.Specific): List<Type.Specific> {
            // get args in the basic converted type, convert stars if any,
            // and pass them to super raw types

            val mapForConvertingFixedStar = specific
                .args
                .filterIsInstance<Arg.General<*>>()
                .associateBy { it.param.simpleName() }

            val noStarArgs = specific.args
                .map { arg ->
                    when (arg) {
                        is Arg.General<*> -> arg

                        is Arg.Star -> Arg.Out(
                            type = arg.param.getBoundProto().convertAll(mapForConvertingFixedStar),
                            ksParam = arg.param
                        )
                    }
                }

            val mapForConvertingGeneric = noStarArgs.associateBy { it.param.simpleName() }

            return specific.ksClass
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

                else -> error("")
            }
            .map { it.updateNullability(convertedBasicType.isNullable) }

        listOf(convertedBasicType) + convertedSuperTypes
    }