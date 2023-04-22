package tracer

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(Car::class)
class WheelImpl(override val __Car: Car) : Wheel, WheelImplTracer {
    val rim = Rim()
    val tire = Tire().apply { inflate() }

    private val revolvingSpeed get() = `__Int_˚Car_Engine_revolvingSpeed`

    override val speed: Int get() = (2 * Math.PI * tire.outerRadius * revolvingSpeed / 120).toInt()

    override fun rotate(){
        println("Wheel rotated")
    }

    override val _WheelImpl: WheelImpl get() = this
}