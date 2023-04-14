package org.koin.example

import org.koin.example.trace._Heater_CoffeeShop_heater
import org.koin.example.trace._Pump_CoffeeShop_pump

context (org.koin.example.trace.CoffeeShopTracer)
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