package example

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class TypeSymbolsSample {
    val lambda: suspend (MutableMap<in String, out CharSequence>) -> Comparable<*>? = TODO()
}