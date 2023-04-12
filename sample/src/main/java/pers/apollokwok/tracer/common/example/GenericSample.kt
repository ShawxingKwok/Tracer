package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.GenericSampleTracer
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

@Tracer.Root
class GenericSample<K: CharSequence, V> : GenericSampleTracer{
    val k: K = TODO()
    val v: V = TODO()
    val kv: Map<K, V> = TODO()

    @Suppress("NonAsciiCharacters")
    override val `_GenericSample‹↓CharSequence，↓Any？›`: GenericSample<out CharSequence, out Any?> get() = this
}