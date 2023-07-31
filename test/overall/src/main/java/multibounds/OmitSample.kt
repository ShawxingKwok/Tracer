package multibounds

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
abstract class OmitSample {
    context(String)
    @Tracer.Omit
    val i: Int get() = length

    val x: Int get() = TODO()
}