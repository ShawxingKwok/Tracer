package multibounds

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.LegTracer

@Tracer.Nodes(Human::class)
class Leg(override val __Human: Human) : LegTracer, Organ(){
    override val _Leg: Leg get() = this
}