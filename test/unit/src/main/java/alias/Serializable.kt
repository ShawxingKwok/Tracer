package alias

import pers.shawxingkwok.tracer.Tracer

interface Serializable

typealias Se = Serializable

@Tracer.Root
class Alias {
    val xx: Xx<*> = TODO()
    val se: Se = TODO() 
}

interface XA<T: CharSequence>
interface XB<T: Serializable>

typealias Xx<T> = Map<XA<T>, XB<T>>