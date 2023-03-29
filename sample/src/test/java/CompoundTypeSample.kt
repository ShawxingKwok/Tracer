import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.CompoundTypeSampleTracer
import java.io.Serializable

interface FooImpl<T> : Foo<T>

interface Foo<T>

interface BarImpl<T> : Bar<T>
    where T: Serializable, T: CharSequence

interface Bar<T>

@Tracer.Root
class CompoundTypeSample<T> : CompoundTypeSampleTracer
    where T: CharSequence, T: Serializable
{
    val foo: FooImpl<T> = TODO()
    val bar: BarImpl<*> = TODO()

    override val _CompoundTypeSample: CompoundTypeSample<*> = this
}