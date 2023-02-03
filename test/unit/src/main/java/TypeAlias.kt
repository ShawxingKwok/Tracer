private interface T_In<in T: CharSequence>{
    fun set(t: T){}
}

private typealias T_InImpl<T> = T_In<T>

// out -> in : !
//private lateinit var t: T_InImpl<out String>
// * -> in : Nothing

private interface T_Out<out T: CharSequence>{
    fun get(): T = TODO()
}
private typealias T_OutImpl<T> = T_Out<T>
// in -> out : !
private lateinit var t: T_OutImpl<String>
// * -> out :

private fun main() {
    t.get()
}