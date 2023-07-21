package car.general

class Car(
    horsepower: Int,
    engineCapacity: Int,
    outerTireRadius: Int,
    innerTireRadius: Int,
) {
    private val wheels: List<Wheel> = List(4){
        val tire = Tire(outerTireRadius, innerTireRadius)
        tire.inflate()
        val rim = Rim(innerTireRadius)
        WheelImpl(outerTireRadius, innerTireRadius, rim, tire)
    }

    private val engine = Engine(horsepower, engineCapacity, wheels)

    init {
        wheels.forEach { (it as WheelImpl).engine = engine }
    }

    fun drive() {
        engine.start()
        repeat(5) {
            engine.speedUp()
        }
        println("\nDriving at ${wheels.first().speed} miles per hour")
    }
}