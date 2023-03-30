package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.TraceEndSampleTracer

@Tracer.Root
class TraceEndSample {
    val rootCarrier = RootCarrier()
    val nodesCarrier = NodesCarrier()
    val tipCarrier = TipCarrier()

    val myInterface: Interface = TODO()
    val myEnum: Enum = TODO()
    val myObject: Object = TODO()

    val nullable: Nullable? = TODO()

    val foreign1: Array<Int> = TODO()
    val foreign2: MyJava = TODO() // class MyJava is written with java in this module.

    val rebuilt1: NonEmptyConstructor = TODO()
    val rebuilt2: Generic<Int> = TODO()
    val rebuilt3: Abstract = TODO()
    val rebuilt4: Open = TODO()
}

@Tracer.Root class RootCarrier{ val x = 1 }
@Tracer.Nodes(RootCarrier::class) class NodesCarrier{ val x = 1 }
@Tracer.Tip class TipCarrier{ val x = 1 }

interface Interface{ val x get() = 1 }
enum class Enum{ A, B, C }
object Object{ val x = 1 }

class Nullable{ val x = 1 }

class NonEmptyConstructor{
    constructor()
    constructor(i: Int) : this()

    val x = 1
}

class Generic<T>{ val x = 1 }
abstract class Abstract{ val x = 1 }
open class Open{ val x= 1 }