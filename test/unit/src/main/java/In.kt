interface In<in T>{
    fun setInT(t: T): Unit = TODO()
    fun getInT(): @UnsafeVariance T = TODO()
}