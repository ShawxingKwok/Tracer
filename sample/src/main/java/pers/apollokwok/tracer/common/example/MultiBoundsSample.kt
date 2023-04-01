package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.Serializable

interface MyList<T> : List<T>
    where T: Serializable, T: CharSequence

interface A
interface B

//val myList: MyList<*> = TODO()

@Tracer.Root
class MultiBoundsSample<T>
    where T: A, T: B
{
    lateinit var t: T
}