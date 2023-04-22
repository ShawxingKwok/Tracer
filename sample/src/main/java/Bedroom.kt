import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(House::class)
class Bedroom(override val __House: House) : BedroomTracer{
    val window = Window()
    val bed = Bed()

    private val wifiRouter get() = `__WifiRouter_˚House_LivingRoom_wifiRouter`

    override val _Bedroom: Bedroom get() = this
}

context (BedroomTracer)
class Window{
    private val door get() = `__Door_˚House_House_door`
    private val pillows get() = `_Pair‹Pillow，Pillow›_Bed_pillows`
}

context (BedroomTracer)
class Bed{
    val pillows = Pillow() to Pillow()
    val quilt = Quilt()

    private val wifiRouter get() = `__WifiRouter_˚House_LivingRoom_wifiRouter`
    private val window get() = _Window_Bedroom_window
}

context (BedroomTracer)
class Pillow

context (BedroomTracer)
class Quilt