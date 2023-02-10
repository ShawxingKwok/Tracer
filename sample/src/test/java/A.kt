import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.ATracer

@Tracer.Root
class A : ATracer{
    override val _A: A get() = this
    val e = 1
}