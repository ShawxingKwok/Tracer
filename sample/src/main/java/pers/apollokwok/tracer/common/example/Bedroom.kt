package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.*
import pers.apollokwok.tracer.common.generated.BedroomTracer

@Tracer.Nodes(House::class)
class Bedroom(override val __House: House) : BedroomTracer{
    val window = Window()
    val bed = Bed()

    override val _Bedroom: Bedroom get() = this
}

context (BedroomTracer)
class Window{
    private val door get() = `__Door_˚House_House_door`
    private val pillows get() = `_Pair‹Pillow，Pillow›_Bed_pillows`
}

context (BedroomTracer)
class Bed{
    val pillows = Pillow(_Bedroom) to Pillow(_Bedroom)
    val quilt = Quilt()
}

context (BedroomTracer)
class Quilt{
    private val window get() = _Window_Bedroom_window
    private val door get() = `__Door_˚House_House_door`
}

@Tracer.Nodes(Bedroom::class)
class Pillow(override val __Bedroom: Bedroom) : PillowTracer{
    val case = Case()
    val cotton = Cotton()

    override val _Pillow: Pillow = this
}

context (PillowTracer)
class Case

context (PillowTracer)
class Cotton{
    private val quilt get() = `__Quilt_˚Bedroom_Bed_quilt`
    private val wifiRouter get() = `__WifiRouter_˚House_LivingRoom_wifiRouter`
}