package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.TestTracer

interface JJ<T>

@Tracer.Root
class Test <T: MyClass?, V> : TestTracer, JJ<T> {
//    var t: T = TODO()
    val k = K()
    override val _Test: Test<*, *> get() = this
}

open class MyClass
class MySubClass : MyClass()

class K{
    val mySubClass = MySubClass()
}

