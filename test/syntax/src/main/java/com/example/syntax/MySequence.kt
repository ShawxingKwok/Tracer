//package com.example.mytest.sequence
//
//import kotlin.time.ExperimentalTime
//import kotlin.time.measureTime
//
//class MySequence<T>(
//    private val origin: Iterable<T>,
//): Iterator<T>{
//    private val actions = mutableListOf<Action>()
//    sealed interface Action
//
//    fun interface Predicate<T> : Action{
//        fun invoke(element: T): Boolean
//    }
//    fun interface Transform<T, R> : Action{
//        fun invoke(element: T): R
//    }
//
//    fun filter(predicate: Predicate<T>): MySequence<T>{
//        actions += predicate
//        return this
//    }
//
//    fun <R> map(transform: Transform<T, R>): MySequence<R>{
//        actions += transform
//        return this as MySequence<R>
//    }
//
//    inline fun count(predicate: (T)->Boolean): Int{
//        var sum = 0
//        forEach {
//            if (predicate(it))
//                sum++
//        }
//        return sum
//    }
//
//    private var next: Any? = null
////    T ODO: change it to get() =
//    private val iterator = origin.iterator()
//
//    override fun hasNext(): Boolean{
//        if (!iterator.hasNext())
//            return false
//
//        next = iterator.next()
//
//        actions.forEach { action ->
//            when(action){
//                is Predicate<*> ->
//                    if (!(action as Predicate<T>).invoke(next as T))
//                        return hasNext()
//                is Transform<*, *> ->
//                    next = (action as Transform<T, *>).invoke(next as T)
//            }
//        }
//
//        return true
//    }
//
//    override fun next(): T = next as T
//}
//
//fun mySequence(range: IntRange) {
//    MySequence(range)
//        .filter { it % 3 != 0 }
//        .filter { it % 4 != 0 }
//        .filter { it % 5 != 0 }
//        .map { it * 2 }
//        .filter { it % 6 != 0 }
//        .filter { it % 7 != 0 }
//        .filter { it % 8 != 0 }
//        .count { it % 9 != 0 }
//        .let(::println)
//}
//
//fun general(range: IntRange){
//    range.count {
//         it % 3 != 0
//         && it % 4 != 0
//         &&  it % 5 != 0
//         &&  it % 6 != 0
//         &&  it % 7 != 0
//         &&  it % 8 != 0
//         &&  it % 9 != 0
//    }.let(::println)
//}
//@OptIn(ExperimentalTime::class)
//fun main() {
//    val range = (0..3000_000)
//    measureTime {
//        general(range)
//    }
//    .let(::println)
//
//    measureTime {
//        mySequence(range)
//    }
//    .let(::println)
//}