package multibounds

import com.example.testlib.Earth
import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(Human::class)
class Leg(override val __Human: Human) : LegTracer, Organ<Any>(){
    override val _Leg: Leg get() = this

    override val __Earth: Earth
        get() = TODO("Not yet implemented")
}