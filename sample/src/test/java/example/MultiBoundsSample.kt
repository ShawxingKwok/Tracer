
package example

import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.Serializable

interface Sub<T> : Super<T>
interface Super<T>

@Tracer.Root
class MultipleBoundsSample<T> : MultipleBoundsSampleTracer
    where T: Serializable, T: CharSequence
{
    lateinit var sub: Sub<T>

    @Suppress("NonAsciiCharacters")
    override val `_MultipleBoundsSample‹↓‹Serializable，CharSequence››`: MultipleBoundsSample<*> get() = this
}