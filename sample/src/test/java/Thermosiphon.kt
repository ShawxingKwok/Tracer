package org.koin.example

import pers.apollokwok.tracer.common.generated.CoffeeShopTracer
import pers.apollokwok.tracer.common.generated._Heater_CoffeeShop_heater

context (CoffeeShopTracer)
class Thermosiphon : Pump {
    private val heater get() = _Heater_CoffeeShop_heater

    override fun pump() {
        if (heater.isHot) {
            println("=> => pumping => =>")
        }
    }
}