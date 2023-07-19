package example

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class House : HouseTracer {
    val masterBedroom = Bedroom(_House)
    val secondaryBedroom = Bedroom(_House)
    val door = Door()
    val livingRoom = LivingRoom()

    override val _House: House get() = this
}

context (HouseTracer)
class Door

context (HouseTracer)
class LivingRoom{
    val wifiRouter = WifiRouter()
}

context (HouseTracer)
class WifiRouter