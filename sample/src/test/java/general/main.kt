package general

import kotlin.system.measureTimeMillis

fun main(){
    val car: Car

    val modelingDuration = measureTimeMillis {
        car = Car(150, 1400, 35, 30)
    }

    val drivingStartDuration = measureTimeMillis { car.drive() }

    println("\nIt takes $modelingDuration ms to model.")
    println("It takes $drivingStartDuration ms to start driving.")
}