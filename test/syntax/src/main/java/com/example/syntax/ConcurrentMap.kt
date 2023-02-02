package com.example.syntax

import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

val concurrentMap = ConcurrentHashMap<Int, Int>()
val map = mutableMapOf<Int, Int>()

fun main(){
    val times = 1000_0000

    measureTimeMillis {
        repeat(times){
            map[it] = it
        }
    }
    .let(::println)


    measureTimeMillis {
        repeat(times){
            concurrentMap[it] = it
        }
    }
    .let(::println)

    measureTimeMillis {
        repeat(times){
            map[it]
        }
    }
    .let(::println)


    measureTimeMillis {
        repeat(times){
            concurrentMap[it]
        }
    }
    .let(::println)
}