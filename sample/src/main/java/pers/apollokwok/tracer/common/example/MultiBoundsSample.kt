package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.Serializable

interface MyList<T> : List<T>
    where T: Serializable, T: CharSequence

@Tracer.Root
class MultiBoundsSample {
    val myList: MyList<*> = TODO()
}