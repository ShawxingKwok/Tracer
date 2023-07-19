package example

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class X : XTracer {
    val s = this

    val e = 1
    override val _X: X = this
}

@Tracer.Nodes(X::class)
abstract class Y : YTracer

@Tracer.Root
class YIMpl : Y(), YIMplTracer {
    override val _YIMpl: YIMpl
        get() = TODO("Not yet implemented")
    override val __X: X
        get() = TODO("Not yet implemented")

    override val _Y: Y
        get() = super._Y
}