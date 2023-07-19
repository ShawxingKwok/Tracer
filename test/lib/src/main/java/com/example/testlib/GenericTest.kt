package com.example.testlib

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class GenericTest<T> : GenericTestTracer {
    lateinit var list: List<T>

    override val `_GenericTest‹↓T-Any？›`: GenericTest<out Any?>
        get() = TODO("Not yet implemented")
}