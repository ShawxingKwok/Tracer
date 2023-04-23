package example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class GenericSample<K: CharSequence, V> : GenericSampleTracer {
    val k: K = TODO()
    val v: V = TODO()
    val kv: Map<K, V> = TODO()

    @Suppress("NonAsciiCharacters")
    override val `_GenericSample‹↓K-CharSequence，↓V-Any？›`: GenericSample<out CharSequence, out Any?>
        get() = TODO("Not yet implemented")
}