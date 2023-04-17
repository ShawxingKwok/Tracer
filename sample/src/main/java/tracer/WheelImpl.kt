package tracer

context (CarTracer)
class WheelImpl : Wheel {
    private val rim = Rim()
    private val tire = Tire().apply { inflate() }

    private val revolvingSpeed get() = _Int_Engine_revolvingSpeed

    override val speed: Int get() = (2 * Math.PI * tire.radius * revolvingSpeed / 120).toInt()

    override fun rotate(){
        println("Wheel rotated")
    }
}