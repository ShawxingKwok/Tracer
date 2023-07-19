package example

import pers.shawxingkwok.tracer.Tracer

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