package pers.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.FamilyTracer

@Tracer.Root
class Family : FamilyTracer{
    val father = Father()
    val mother = Mother()
    val child = Child()
    val house = House()

    override val _Family: Family get() = this
}

context (FamilyTracer)
class Father

context (FamilyTracer)
class Mother{
}

context (FamilyTracer)
class Child

context (FamilyTracer)
class House{
    val westBedroom = Room()
    val eastBedroom = Room()
    val bathroom = Room()
}

class Room

fun main() {
    Family()
}