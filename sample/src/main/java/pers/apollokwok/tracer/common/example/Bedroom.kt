package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.generated._WifiRouter

context (pers.apollokwok.tracer.common.generated.HouseTracer)
class Bedroom{
    private val wifiRouter get() = _WifiRouter
}
