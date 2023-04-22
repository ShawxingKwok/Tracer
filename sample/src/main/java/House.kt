import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class House : HouseTracer{
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

    private val masterBedroom get() = _Bedroom_House_masterBedroom
    private val secondaryBedroom get() = _Bedroom_House_secondaryBedroom
    private val door get() = _Door_House_door
}

context (HouseTracer)
class WifiRouter{
    private val livingRoom get() = _LivingRoom_House_livingRoom
}