package tracer

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class Car(
    val horsepower: Int,
    val engineCapacity: Int,
    val outerTireRadius: Int,
    val innerTireRadius: Int,
) : CarTracer{
    val wheels: List<Wheel> = List(4){ WheelImpl(_Car) }
    val engine = Engine()

    fun drive() {
        engine.start()
        repeat(5) { engine.speedUp() }
        println("\nDriving at ${wheels.first().speed} miles per hour")
    }

    override val _Car: Car get() = this
}