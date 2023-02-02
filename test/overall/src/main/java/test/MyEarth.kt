package test

import com.example.testlib.Earth
import com.example.testlib.Nature
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.MyEarthTracer

@Tracer.Nodes(MyNature::class)
class MyEarth(override val __MyNature: MyNature) : Earth(__MyNature), MyEarthTracer {
    override val _MyEarth: MyEarth get() = this
    override val __Nature: Nature get() = __MyNature

    val human = Human(this)
}