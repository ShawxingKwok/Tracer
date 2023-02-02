package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.NatureTracer

@Tracer.Root
abstract class Nature : NatureTracer {
    abstract val earth: Earth
}