package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class GenericTest<T> : GenericTestTracer {
    lateinit var list: List<T>

    override val `_GenericTest‹↓T-Any？›`: GenericTest<out Any?>
        get() = TODO("Not yet implemented")
}