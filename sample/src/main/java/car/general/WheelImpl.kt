package car.general

class WheelImpl(
    private val outerRadius: Int,
    private val innerRadius: Int,
    private val rim: Rim,
    private val tire: Tire,
) : Wheel {
    lateinit var engine: Engine

    override val speed: Int get() = (2 * Math.PI * outerRadius * engine.revolvingSpeed / 120).toInt()

    override fun rotate(){
        println("Wheel rotated")
    }
}