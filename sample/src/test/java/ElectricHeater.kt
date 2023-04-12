package org.koin.example

import pers.apollokwok.tracer.common.generated.CoffeeShopTracer

context (CoffeeShopTracer)
class ElectricHeater : Heater {
    override var isHot: Boolean = false
        private set

    override fun on() {
        println("~ ~ ~ heating ~ ~ ~")
        isHot = true
    }

    override fun off() {
        isHot = false
    }
}