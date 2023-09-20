import car.tracer.*
import io.mockk.*
import kotlin.test.Test

class EngineTest {
    val carTracer = mockk<CarTracer>()
    val wheels = List(4){ mockk<Wheel>(relaxUnitFun = true) }
    init {
        every { carTracer.`_List‹Wheel›_Car_wheels` }.returns(wheels)
        every { carTracer._Int_Car_horsepower }.returns(100)
        every { carTracer._Int_Car_engineCapacity }.returns(100)
    }

    val engine = carTracer.run { Engine() }.let(::spyk)

    @Test
    fun start(){
        engine.start()
        verifyAll {
            wheels.forEach { it.rotate() }
        }
    }

    @Test
    fun speedUp(){
        engine.speedUp()
        assert(engine.revolvingSpeed == 10)
    }

    @Test
    fun slowDown(){
        engine.slowdown()
        assert(engine.revolvingSpeed == 0)
    }

    @Test
    fun speedUpAndSlowDown(){
        engine.speedUp()
        engine.speedUp()
        engine.slowdown()
        assert(engine.revolvingSpeed == 10)
    }
}