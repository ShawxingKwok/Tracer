package a

import a.trace.ATracer
import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class A<T: Any> : ATracer{
}