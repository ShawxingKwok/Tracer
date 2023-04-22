package tracer

import kotlin.system.measureTimeMillis

fun main() {
    val duration = measureTimeMillis {
        Car(150, 1400, 35, 30).drive()
    }
    println("\nIt takes $duration ms to start.")
}