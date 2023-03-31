package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

@Tracer.Root
class GenericSample<T: CharSequence, V>
    where V: Serializable, V: CoroutineContext
{
    val singleBound: T = TODO()
    val multipleBounds: V = TODO()

    // covariance may be needed.
    val arr: Array<T> = TODO()
    val list: List<T> = TODO()
}