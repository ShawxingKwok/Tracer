package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.GenericTestTracer

@Tracer.Root
class GenericTest<T> : GenericTestTracer {
    lateinit var list: List<T>
    override val _GenericTest: GenericTest<*> get() = this
}