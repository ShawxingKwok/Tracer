package com.example.testlib

import pers.shawxingkwok.tracer.Tracer

interface JJ<T>

@Tracer.Root
class Test <T: MyClass?, V> : TestTracer, JJ<T> {
//    var t: T = TODO()
    val k = K()
    override val `_Test‹↓T-MyClass？，↓V-Any？›`: Test<out MyClass?, out Any?>
        get() = TODO("Not yet implemented")
}

open class MyClass
class MySubClass : MyClass()

class K{
    val mySubClass = MySubClass()
}

