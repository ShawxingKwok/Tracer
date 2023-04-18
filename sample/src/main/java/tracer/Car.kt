package tracer

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class Car(
    val horsePower: Int,
    val engineCapacity: Int,
    val tireRadius: Int,
) : CarTracer{
    val wheels: List<Wheel> = List(4){ WheelImpl() }
    val engine = Engine()

    fun drive() {
        engine.start()
        repeat(5) { engine.speedUp() }
        println("\nDriving at ${wheels.first().speed} miles per hour")
    }

    override val _Car: Car get() = this
}