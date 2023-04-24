package example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class TypeSymbolsSample {
    val lambda: suspend (MutableMap<in String, out CharSequence>) -> Comparable<*>? = TODO()
}