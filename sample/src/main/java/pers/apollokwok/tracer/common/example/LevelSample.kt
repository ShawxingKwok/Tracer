package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
abstract class LevelSample {
    val bar = Bar()
}

class Bar{ val x = 1 }

@Tracer.Root
class LevelSampleImpl : LevelSample(){
    val baz = Baz()
}

class Baz{
    val bar = Bar()
}