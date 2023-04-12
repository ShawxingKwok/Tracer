package org.koin.example

import pers.apollokwok.tracer.common.generated.CoffeeShopTracer
import pers.apollokwok.tracer.common.generated._Heater_CoffeeShop_heater
import pers.apollokwok.tracer.common.generated._Pump_CoffeeShop_pump

context (CoffeeShopTracer)
class CoffeeMaker {
    private val pump get() = _Pump_CoffeeShop_pump
    private val heater get() = _Heater_CoffeeShop_heater

    fun brew() {
        heater.on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.off()
    }
}