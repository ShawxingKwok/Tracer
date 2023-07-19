package multibounds

import com.example.testlib.Earth
import pers.shawxingkwok.tracer.Tracer

@Tracer.Nodes(MyEarth::class)
class Human(override val `__MyEarth‹↓T-Any？›`: MyEarth<*>) : HumanTracer {
    override val __Earth: Earth get() = `__MyEarth‹↓T-Any？›`
    override val _Human: Human get() = this
}