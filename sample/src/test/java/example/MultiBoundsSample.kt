
package example

import pers.shawxingkwok.tracer.Tracer
import java.io.Serializable

interface Sub<T> : Super<T>
interface Super<T>

@Tracer.Root
class MultipleBoundsSample<T> : MultipleBoundsSampleTracer
    where T: Serializable, T: CharSequence
{
    lateinit var sub: Sub<T>

    @Suppress("NonAsciiCharacters")
    override val `_MultipleBoundsSample‹↓T-‹Serializable，CharSequence››`: MultipleBoundsSample<*>
        get() = this
}