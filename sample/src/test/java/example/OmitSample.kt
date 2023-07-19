package example

import pers.shawxingkwok.tracer.Tracer
import java.io.Serializable

@Tracer.Root
abstract class OmitSample<T: CharSequence?> : @Tracer.Omit List<T & Any>, Serializable {
    @Tracer.Omit
    lateinit var defNotNull: T & Any

    context (String)
    @Tracer.Omit
    val x get() = length
}