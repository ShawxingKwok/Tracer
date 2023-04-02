package generic

import pers.apollokwok.tracer.common.annotations.Tracer

typealias S = java.io.Serializable

@Tracer.Root
class X<T: S, V: T> {
    val v: V = TODO()
    val vs: List<V> = TODO()
    val t: T = TODO()
}