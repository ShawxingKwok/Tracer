package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.BedroomTracer
import pers.apollokwok.tracer.common.generated.`_Pair‹Pillow，Pillow›_Bed_pillows`
import pers.apollokwok.tracer.common.generated.`__Door_˚House_House_door`

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
    val pillows = Pillow() to Pillow()
    val quilt = Quilt()
}

context (BedroomTracer)
class Pillow

context (BedroomTracer)
class Quilt