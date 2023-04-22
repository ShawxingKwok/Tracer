package k

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class Import {
    val a = s.A()
    val b = s.A.B()
    val _a = t.A()
    val _b = t.A.B()
}