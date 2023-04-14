package org.koin.example

import org.koin.example.trace._Heater_CoffeeShop_heater

context (org.koin.example.trace.CoffeeShopTracer)
class Thermosiphon : Pump {
    private val heater get() = _Heater_CoffeeShop_heater

    override fun pump() {
        if (heater.isHot) {
            println("=> => pumping => =>")
        }
    }
}