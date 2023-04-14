package org.koin.example

context (org.koin.example.trace.CoffeeShopTracer)
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