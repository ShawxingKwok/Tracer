package org.koin.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.CoffeeShopTracer
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@Tracer.Root
class CoffeeShop : CoffeeShopTracer{
    val maker = CoffeeMaker()
    val pump: Pump = Thermosiphon()
    val heater: Heater = ElectricHeater()

    override val _CoffeeShop: CoffeeShop = this
}

@OptIn(ExperimentalTime::class)
fun main() {
    val duration = measureTime { CoffeeShop().maker.brew() }
    println("Got coffee in $duration ms")
}