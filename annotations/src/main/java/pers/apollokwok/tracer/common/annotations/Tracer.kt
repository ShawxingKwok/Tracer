package pers.apollokwok.tracer.common.annotations

import kotlin.reflect.KClass

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
     * Inner elements wouldn't be explored in the trace.
     * Data classes, foreign classes, and those with rebuilt symbols are considered as [Tips] by default.
     */
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Tips

    /**
     * Is used on super types or traceable properties which you want to omit in the trace.
     * Generally, these properties own some new syntaxes unsupported by `ksp` like `context receiver` at present.
     * ```
     * @Tracer.Root
     * class Sample<T> : @Tracer.Omit List<T & Any>{
     *     @Tracer.Omit
     *     context(String)
     *     val x: T get() = ...
     * }
     * @since 1.1.10 with Kotlin 1.7.20
     */
    @Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class Omit
}