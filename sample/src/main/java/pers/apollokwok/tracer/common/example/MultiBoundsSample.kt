package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.CompoundTypeSampleTracer
import java.io.Serializable

interface Sub<T> : Super<T>
interface Super<T>

@Tracer.Root
class CompoundTypeSample<T> : CompoundTypeSampleTracer
    where T: Serializable, T: CharSequence
{
    lateinit var sub: Sub<T>

    override val _CompoundTypeSample: CompoundTypeSample<*> = this
}