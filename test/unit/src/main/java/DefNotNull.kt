private class DefNotNull<T> {
    val j: DefNotNull<T & Any> = TODO()
    //    val js : List<J<T & Any, *>> = TODO()
    lateinit var t: T & Any
    fun foo(){}
}