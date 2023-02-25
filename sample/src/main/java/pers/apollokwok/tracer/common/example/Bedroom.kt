package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.*
import pers.apollokwok.tracer.common.generated.BedroomTracer

@Tracer.Nodes(House::class)
class Bedroom(override val __House: House) : BedroomTracer {
    private val wifiRouter get() = `__WifiRouter_˚House`

    val bed = Bed()
    val window = Window()

    override val _Bedroom: Bedroom = this
}

context (BedroomTracer)
class Bed{
    val pillows = Pillow() to Pillow()
    val quilt = Quilt()
}

context (BedroomTracer)
class Pillow

context (BedroomTracer)
class Quilt{
    private val window get() = _Window
    private val door get() = `__Door_˚House`
}

context (BedroomTracer)
class Window