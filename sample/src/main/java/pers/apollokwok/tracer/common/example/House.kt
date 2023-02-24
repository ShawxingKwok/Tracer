package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.HouseTracer
import pers.apollokwok.tracer.common.generated._WifiRouter

@Tracer.Root
class House : HouseTracer{
    val bedroom = Bedroom()
    val door = Door()
    val livingRoom = LivingRoom()

    override val _House: House = this
}

context (HouseTracer)
class Bedroom{
    private val wifiRouter get() = _WifiRouter
}

context (HouseTracer)
class Door

context (HouseTracer)
class LivingRoom{
    val wifiRouter = WifiRouter()
}

context (HouseTracer)
class WifiRouter