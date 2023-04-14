import a.A
import a.trace._ATracer
import b.N
import b.trace.NTracer
import pers.apollokwok.tracer.common.annotations.Tracer
import trace.ZzTracer
import x.C

@Tracer.Nodes(C.X::class)
class Zz(override val `__C․X‹↓‹Serializable，CharSequence››`: C.X<*>) : N(), ZzTracer{
    override val _Zz: Zz
        get() = TODO("Not yet implemented")

    override val `__A‹↓Any›`: A<out Any> get() = _ATracer.`_A‹↓Any›`
}