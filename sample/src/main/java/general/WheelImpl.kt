package general

class WheelImpl(
    private val rim: Rim,
    private val tire: Tire,
) : Wheel {
    lateinit var engine: Engine

    override val speed: Int get() = (2 * Math.PI * tire.radius * engine.revolvingSpeed / 120).toInt()

    override fun rotate(){
        println("Wheel rotated")
    }
}