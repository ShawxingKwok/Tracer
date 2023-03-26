package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer

interface FL

@Tracer.Root
class FullNameSample : FL{
    val s = "S"
    val house = House()
}