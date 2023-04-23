package multibounds

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class Organ<T> : OrganTracer{
    val s = 12
}