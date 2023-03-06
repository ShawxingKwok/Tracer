package pers.apollokwok.tracer.common.annotations

import kotlin.reflect.KClass

@Target
public annotation class Tracer{
    /**
     * **See** [Tracer Annotations](https://apollokwok.github.io/TracerTutorial/usage/annotations/)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Root

    /**
     * **See** [Tracer Annotations](https://apollokwok.github.io/TracerTutorial/usage/annotations/)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Nodes(val context: KClass<*>)

    /**
     * **See** [Tracer Annotations](https://apollokwok.github.io/TracerTutorial/usage/annotations/)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tip

    /**
     * **See** [Tracer Annotations](https://apollokwok.github.io/TracerTutorial/usage/annotations/)
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Omit
}