package test

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.OrganTracer

@Tracer.Root
abstract class Organ : OrganTracer{
    val s = 12
}