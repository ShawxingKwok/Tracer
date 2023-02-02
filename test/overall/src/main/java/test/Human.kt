package test

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.HumanTracer

@Tracer.Nodes(MyEarth::class)
class Human(override val __MyEarth: MyEarth) : HumanTracer {
    override val _Human get() = this

    val legs = listOf(Leg(this), Leg(this))
}