import pers.apollokwok.tracer.common.annotations.Tracer

private class JK<T: V, V: CharSequence?> {
    val j: JK<T & Any, *> = TODO()
    val js: List<JK<T & Any, *>> = TODO()
    lateinit var t: T & Any
    fun foo() {}
}