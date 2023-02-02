package test

import pers.apollokwok.tracer.common.annotations.Tracer

typealias FG<T> = List<T>

@Tracer.Root
class G<T>{
}

internal interface KLK

class FK : KLK{
    var sa: SA? = null
}

class SA{
    val ss = listOf(1)
}