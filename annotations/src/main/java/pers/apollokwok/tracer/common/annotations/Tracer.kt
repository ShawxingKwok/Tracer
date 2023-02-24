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
     * Inner elements wouldn't be explored in the trace. Data classes are considered as [Tips] by default.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tips

    /**
     * [Declare] is used on visible properties with `get() =` which is omitted by default.
     * ```
     * class Sample{
     *     private val _list = mutableListOf<Int>()
     *
     *     @Tracer.Declare
     *     val list get() = _list
     * }
     * ```
     *
     * [Declare]`(`false`)` is used on super types or traceable properties which you want to omit in the trace.
     * Generally, these properties own some new syntaxes unsupported by `ksp` like `context receiver` at present.
     * ```
     * @Tracer.Root
     * class Sample<T> : @Tracer.Declare(false) List<T & Any>{
     *     @Tracer.Declare(false)
     *     context(String)
     *     val x: T get() = ...
     * }
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Declare(val enabled: Boolean = true)
}