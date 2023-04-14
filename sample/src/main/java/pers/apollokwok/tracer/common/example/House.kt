package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.example.trace.HouseTracer

@Tracer.Root
class House : HouseTracer {
    val masterBedroom = Bedroom(_House)
    val secondaryBedroom = Bedroom(_House)
    val door = Door()
    val livingRoom = LivingRoom()

    override val _House: House get() = this
}

context (pers.apollokwok.tracer.common.example.trace.HouseTracer)
class Door

context (HouseTracer)
class LivingRoom{
    val wifiRouter = WifiRouter()
}

context (HouseTracer)
class WifiRouter