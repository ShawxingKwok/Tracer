package multibounds

import com.example.testlib.Earth
import com.example.testlib.Nature
import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Nodes(MyNature::class)
class MyEarth<T>(override val __MyNature: MyNature) : Earth(__MyNature), MyEarthTracer {
    //    val human = Human(this)

    override val `_MyEarth‹↓T-Any？›`: MyEarth<*> get() = this
    override val __Nature: Nature get() = __MyNature
}