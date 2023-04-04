package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class VarianceSample<T: java.io.Serializable> {
    val v: Comparable<T> = TODO()
}