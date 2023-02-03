interface Out<out T> {
    fun getOutT(): T
    fun setOutT(t: @UnsafeVariance T)
}