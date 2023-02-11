package pers.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.FamilyTracer
import pers.apollokwok.tracer.common.generated.RoomTracer
import java.awt.Window

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
class Mother

context (FamilyTracer)
class Child

context (FamilyTracer)
class House{
    val westBedroom = Room(_Family)
    val eastBedroom = Room(_Family)
    val bathroom = Room(_Family)
}

@Tracer.Nodes(Family::class)
class Room(override val __Family: Family) : RoomTracer{
    val floor = Floor()
    val window = Window()

    fun open(){
        println("open")
    }

    override val _Room: Room get() = this
}

context (RoomTracer)
class Window

context (RoomTracer)
class Floor

fun main() {
    Family()
}