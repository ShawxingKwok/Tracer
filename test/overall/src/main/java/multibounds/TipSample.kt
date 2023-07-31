package multibounds

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class TipSample {
    val tip = Tip()
}

@Tracer.Tip
class Tip{
    val x = 1
}