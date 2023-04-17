package general

class Engine(
    private val horsePower: Int,
    private val engineCapacity: Int,
    private val wheels: List<Wheel>,
) {
    var revolvingSpeed = 0
        private set

    fun start() {
        println(
            """

             Engine started
             Horsepower: $horsePower
             Engine capacity: $engineCapacity
             
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