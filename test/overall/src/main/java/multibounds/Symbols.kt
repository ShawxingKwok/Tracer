package multibounds

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class Symbols<T> {
    @Tracer.Omit val t: T & Any = TODO()

    val lambda: ()->Unit = TODO()
}