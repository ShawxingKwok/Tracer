package pers.apollokwok.tracer.common.annotations

import kotlin.reflect.KClass

/**
 * A single [Tracer] is forbidden to use.
 */
@Target
public annotation class Tracer{
    /**
     * Inner traceable elements would be provided via extensional properties in generated files.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Root

    /**
     * Inner traceable elements would be provided via extensional properties in generated files.
     * [Nodes] means the annotated class would be built for multiple times inside its [context].
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    public annotation class Nodes(val context: KClass<*>)

    /**
     * Tells elements inside the annotated class wouldn't be traced.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tips

    /**
     * Properties without fields, not delegated, and not abstract would be omitted by default, but can be
     * declared if they are annotated with [Declare].
     * ```
     * class Sample{
     *     private val _list = mutableListOf<Int>()
     *
     *     @Tracer.Declare
     *     val list get() = _list
     * }
     * ```
     * [Declare]`(`false`)` is used on super types or traceable properties which would be omitted when being traced.
     * ```
     * class Sample : @Tracer.Declare(false) Serializable{
     *     @Tracer.Declare(false)
     *     val x = 1
     * }
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Declare(val enabled: Boolean = true)
}