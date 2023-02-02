package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.AnimalTracer

@Tracer.Root
abstract class Animal : AnimalTracer {
    abstract val name: String
    open val heart: Heart = Heart()
    val vessels = setOf(Vessel())
}

open class Heart{
    val vessels = setOf(Vessel())
    val s = 1

}

class Vessel
