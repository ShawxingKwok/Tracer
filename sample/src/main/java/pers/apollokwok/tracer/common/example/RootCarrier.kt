package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class RootCarrier {
    val tipCarrier = TipCarrier()
}

@Tracer.Tip
class TipCarrier{
    val x = 1
}