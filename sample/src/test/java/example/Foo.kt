package example

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class Foo<T> : FooTracer
    where T: A, T: B
{
    lateinit var t: T

    @Suppress("NonAsciiCharacters")
    override val `_Foo‹↓T-‹A，B››`: Foo<*> get() = this
}

lateinit var foo: Foo<*>

val a: A = foo.t
//val b: B = foo.t
val _b: B = foo.t as B


interface A
interface B