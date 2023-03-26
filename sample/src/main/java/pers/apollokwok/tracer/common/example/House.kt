package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.*
import pers.apollokwok.tracer.common.generated.HouseTracer

@Tracer.Root
class House : HouseTracer{
    val bedroom = Bedroom()
    val door = Door()
    val livingRoom = LivingRoom()

    override val _House: House = this
}

context (HouseTracer)
class Bedroom{
    private val wifiRouter get() = _WifiRouter_LivingRoom_wifiRouter
}

context (HouseTracer)
class Door

context (HouseTracer)
class LivingRoom{
    val wifiRouter = WifiRouter()
}

context (HouseTracer)
class WifiRouter



//val masterBedroom = Bedroom(this)
//val secondaryBedroom = Bedroom(this)