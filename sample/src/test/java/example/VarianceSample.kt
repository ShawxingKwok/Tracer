package example

import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class VarianceSample<T: java.io.Serializable> {
    val v: Comparable<T> = TODO()
}