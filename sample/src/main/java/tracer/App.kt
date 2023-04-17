package tracer

import kotlin.system.measureTimeMillis

class App {
    private val car = Car(150, 1400)

    fun start(){
        car.drive()
    }
}

fun main() {
    val duration = measureTimeMillis { App().start() }
    println("\nIt takes $duration ms to start.")
}