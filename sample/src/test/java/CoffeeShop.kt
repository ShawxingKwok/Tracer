package org.koin.example

import org.koin.example.trace.CoffeeShopTracer
import pers.apollokwok.tracer.common.annotations.Tracer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Tracer.Root
class CoffeeShop : CoffeeShopTracer {
    val maker = CoffeeMaker()
    val pump: Pump = Thermosiphon()
    val heater: Heater = ElectricHeater()

    override val _CoffeeShop: CoffeeShop get() = this
}

@OptIn(ExperimentalTime::class)
fun main() {
    val duration = measureTime { CoffeeShop().maker.brew() }
    println("Got coffee in $duration ms")
}