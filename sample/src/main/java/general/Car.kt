package general

class Car(
    horsePower: Int,
    engineCapacity: Int,
) {
    //region initialization requires the right order which is quite long and error-prone in a big project)
    private val wheels: List<Wheel> = List(4){
        val rim = Rim()
        val tire = Tire()
        tire.inflate()
        WheelImpl(rim, tire)
    }

    private val engine = Engine(horsePower, engineCapacity, wheels)
    //endregion

    // solves dependency recycle(wheel and engine need each other)
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