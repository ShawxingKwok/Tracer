package com.example.syntax

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlin.properties.Delegates

suspend fun main() {
    val flow = flowOf(1,2)
    flow.collect(::println)
    flow.collect(::println)
}