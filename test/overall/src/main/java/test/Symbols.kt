package test

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class Symbols<T> {
    val t: T & Any = TODO()

    val lambda: ()->Unit = TODO()
}