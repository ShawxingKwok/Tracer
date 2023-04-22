package tracer

context (CarTracer)
class Engine {
    val horsePower: Int = _Int_Car_horsepower
    val capacity: Int = _Int_Car_engineCapacity
    var revolvingSpeed = 0
        private set

    private val wheels get() = `_List‹Wheel›_Car_wheels`

    fun start() {
        println(
            """

             Engine started
             Horsepower: $horsePower
             Engine capacity: $capacity
             
             """.trimIndent()
        )
        wheels.forEach { it.rotate() }
        println()
    }

    fun speedUp(){
        revolvingSpeed += 10
        println("Speed up")
    }

    fun slowdown(){
        revolvingSpeed -= 10
        println("Slow down")
    }
}