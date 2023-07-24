package pers.shawxingkwok.tracer

import kotlin.reflect.KClass

@Target
public annotation class Tracer {
    /**
     * See [Root](https://shawxingkwok.github.io/ITWorks/docs/jvm/tracer/usage/annotations/#root)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Root

    /**
     * See [Nodes](https://shawxingkwok.github.io/ITWorks/docs/jvm/tracer/usage/annotations/#nodes)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Nodes(val context: KClass<*>)

    /**
     * See [Tip](https://shawxingkwok.github.io/ITWorks/docs/jvm/tracer/usage/annotations/#tip)
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tip

    /**
     * See [Omit](https://shawxingkwok.github.io/ITWorks/docs/jvm/tracer/usage/annotations/#omit)
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Omit
}