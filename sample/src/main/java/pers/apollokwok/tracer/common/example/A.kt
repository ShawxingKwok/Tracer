package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.ABTracer
import pers.apollokwok.tracer.common.generated.JTracer

class A {
    @Tracer.Root
    class B : ABTracer {
        val s = listOf("")

        override val `_A․B`: B
            get() = TODO("Not yet implemented")
    }
}

@Tracer.Nodes(A.B::class)
class J(override val `__A․B`: A.B) : JTracer{
    val house = House()
    val file = File()
    val javafiles = listOf(java.io.File(""))
    val b = A.B()

    init {
        `__A․B`
    }

    override val _J: J get() = this
}


class File