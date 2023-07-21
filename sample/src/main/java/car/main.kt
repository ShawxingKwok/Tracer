package car

import car.tracer.Car
import kotlin.system.measureNanoTime

fun <T> testBenchmark(getCar: ()->T, drive: (T) -> Unit){
    val car: T

    val modelingDuration = measureNanoTime { car = getCar() } / 1000

    val drivingStartDuration = measureNanoTime { drive(car) } / 1000

    """
        It takes $modelingDuration µs to model.
        It takes $drivingStartDuration µs to start driving.
    """.trimIndent()
        .let(::println)
}

fun testTracerCar(){
    testBenchmark(
        getCar = { Car(150, 1400, 35, 30) },
        drive = Car::drive
    )
}

fun testGeneralCar(){
    testBenchmark(
        getCar = {
            car.general.Car(150, 1400, 35, 30)
        },
        drive = car.general.Car::drive
    )
}

// Test a few times to compare.
// Don't test both these together, or it would be affected by the jvm start.
fun main() {
    // testTracerCar()
    testGeneralCar()
}