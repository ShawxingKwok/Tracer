import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class SG: SGTracer{

}


@Tracer.Root
class G<T, V: T> : SG(), GTracer{
    override val `_G‹↓T-Any？，↓V-T-Any？›`: G<*, *> get() = this
}

@Tracer.Root
abstract class SI : SITracer

@Tracer.Root
open class I : SI(), ITracer{
    override val _I: I
        get() = TODO("Not yet implemented")
}

@Tracer.Nodes(G::class)
class SubI(override val `__G‹↓T-Any？，↓V-T-Any？›`: G<*, *>) : I(), SubITracer{
    override val _SubI: SubI
        get() = TODO("Not yet implemented")
}