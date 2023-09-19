import car.tracer.*
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class EngineTest {
    @Test
    fun start(){
        val carTracer = mockk<CarTracer>()
        every { carTracer._Int_Car_horsepower }.returns(100)
        every { carTracer._Int_Car_engineCapacity }.returns(100)

        val wheels = List(4){ mockk<Wheel>() }
        wheels.forEach {
            every { it.rotate() }.returns(Unit)
        }

        every { carTracer.`_List‹Wheel›_Car_wheels` }.returns(wheels)

        val engine = carTracer.run { Engine() }
        engine.start()
        engine.speedUp()
        engine.slowdown()

        assert(engine.revolvingSpeed == 0)
    }
}