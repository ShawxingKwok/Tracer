package example

import pers.shawxingkwok.tracer.Tracer

typealias MyMap = Map<String, Int>

@Tracer.Root
class TypeAliasSample {
    val myMap: MyMap = TODO()
}