package pers.apollokwok.tracer.common.annotations

import kotlin.reflect.KClass

@Target
public annotation class Tracer{
    /**
     * **See** [Root](https://apollokwok.github.io/TracerTutorial/docs/usage/annotations/#root)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Root

    /**
     * **See** [Nodes](https://apollokwok.github.io/TracerTutorial/docs/usage/annotations/#nodes)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Nodes(val context: KClass<*>)

    /**
     * **See** [Tip](https://apollokwok.github.io/TracerTutorial/docs/usage/annotations/#tip)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tip

    /**
     * **See** [Omit](https://apollokwok.github.io/TracerTutorial/docs/usage/annotations/#omit)
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Omit
}