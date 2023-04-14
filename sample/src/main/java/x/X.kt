package x

import pers.apollokwok.tracer.common.annotations.Tracer
import x.trace.C_XTracer

class C {
    @Tracer.Root
    class X <T> : C_XTracer
        where T: java.io.Serializable, T: CharSequence
    {
        override val `_C․X‹↓‹Serializable，CharSequence››`: X<*>
            get() = TODO("Not yet implemented")
    }
}