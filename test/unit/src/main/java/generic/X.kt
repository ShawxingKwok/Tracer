package generic

import pers.shawxingkwok.tracer.Tracer

typealias S = java.io.Serializable

@Tracer.Root
class X<T: S, V> {
    val v: V = TODO()
    val vs: List<V> = TODO()
    val t: T = TODO()
}