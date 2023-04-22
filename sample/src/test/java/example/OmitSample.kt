package example

import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.Serializable

@Tracer.Root
abstract class OmitSample<T: CharSequence?> : @Tracer.Omit List<T & Any>, Serializable {
    @Tracer.Omit
    lateinit var defNotNull: T & Any

    context (String)
    @Tracer.Omit
    val x get() = length
}