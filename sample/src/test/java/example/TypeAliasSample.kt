package example

import pers.apollokwok.tracer.common.annotations.Tracer

typealias MyMap = Map<String, Int>

@Tracer.Root
class TypeAliasSample {
    val myMap: MyMap = TODO()
}