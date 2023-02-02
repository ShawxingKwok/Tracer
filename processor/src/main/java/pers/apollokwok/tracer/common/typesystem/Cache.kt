package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSType
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ktutil.Bug

// save another type opposite in nullability meantime.
internal abstract class Cache<T: Any> private constructor(){
    class Type<T: pers.apollokwok.tracer.common.typesystem.Type<*>> : Cache<T>()
    class Types<T: List<pers.apollokwok.tracer.common.typesystem.Type<*>>> : Cache<T>()

    private val map = mutableMapOf<KSType, T>().alsoRegister()

    inline fun getOrPut(ksType: KSType, getValue: (KSType) -> T): T =
        map.getOrPut(ksType) {
            val value = getValue(ksType)

            val anotherKey =
                if (ksType.isMarkedNullable)
                    ksType.makeNotNullable()
                else
                    ksType.makeNullable()

            @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
            val anotherValue = when (value) {
                is pers.apollokwok.tracer.common.typesystem.Type<*> -> value.updateNullability(!value.isNullable)
                is List<*> -> (value as List<pers.apollokwok.tracer.common.typesystem.Type<*>>)
                    .map { it.updateNullability(!it.isNullable) }
                else -> Bug()
            } as T

            map[anotherKey] = anotherValue

            value
        }
}