package b

import a.A
import b.trace.NTracer
import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(A::class)
abstract class N : NTracer{
}