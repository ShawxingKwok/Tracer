package car.tracer

import pers.shawxingkwok.tracer.Tracer

context (CarTracer)
class Engine {
    var revolvingSpeed = 0
        private set

    @Tracer.Omit val horsePower: Int get() = _Int_Car_horsepower
    @Tracer.Omit val capacity: Int get() = _Int_Car_engineCapacity
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
        if (revolvingSpeed < 0) revolvingSpeed = 0
        println("Slow down")
    }
}