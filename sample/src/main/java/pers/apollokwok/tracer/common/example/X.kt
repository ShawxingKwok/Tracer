package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.XTracer
import java.io.Serializable

@Tracer.Root
class X<T> : XTracer
    where T: CharSequence, T: Serializable
{
    val y = Y()
    val house: House? = House()

    val x: X<*> = TODO()

    override val _X: X<*> = this
}

context (XTracer)
class Y{
    val westHouse = House()
    val eastHouse = House()
}