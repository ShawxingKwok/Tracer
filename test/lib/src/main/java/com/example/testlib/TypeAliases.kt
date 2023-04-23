package com.example.testlib

import pers.apollokwok.tracer.common.annotations.Tracer

internal typealias X<A> = Y<A, Int>
public typealias Y<A, B> = Z<A?, String, B>
private typealias Z<A, B, C> = kotlin.Triple<A, B, C>?

@Tracer.Root
class TypeAliasTest : TypeAliasTestTracer {
    var x: kotlin.Triple<X<Int>, Y<Long, Boolean>, Z<Short, Double, Float?>> = TODO()
    val kk = KK()
    val job = Job()
    override val _TypeAliasTest: TypeAliasTest get() = this
}

class Job

class KK{
    val job: kotlinx.coroutines.Job = kotlinx.coroutines.Job()
    val job2 = Job()
    lateinit var job3: kotlinx.coroutines.Job
}