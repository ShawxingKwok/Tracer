import car.tracer.*
import io.mockk.*
import kotlin.test.Test

class EngineTest {
    val wheels = List(4){ mockk<Wheel>(relaxUnitFun = true) }

    val engine = mockk<CarTracer>().run { spyk(Engine()) }.also {
        every { it.horsePower } returns 100
        every { it.capacity } returns 100
        every { it getProperty "wheels" } returns wheels
    }

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
}