package car.general

class Engine(
    private val horsepower: Int,
    private val capacity: Int,
    private val wheels: List<Wheel>,
) {
    var revolvingSpeed = 0
        private set

    fun start() {
        println(
            """

             Engine started
             Horsepower: $horsepower
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