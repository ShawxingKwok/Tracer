package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer

interface Public<T> : MutableList<T>

private interface Private

@Tracer.Root
abstract class SuperTypeSample<T: CharSequence> :
    SuperTypeSampleTracer, // omitted
    Private, // omitted
    Public<T> // traced with its super type MutableList<CharSequence>
              // then stopped going upward because MutableList is foreign
              // (from generated code, Java files, or other modules)
{
    // it's super type `MutableList<out Any?>` is also traced.
    val publicInts: Public<*> = TODO()
}