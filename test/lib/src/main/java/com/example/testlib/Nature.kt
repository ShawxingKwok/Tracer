package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class Nature : NatureTracer {
    abstract val earth: Earth
}