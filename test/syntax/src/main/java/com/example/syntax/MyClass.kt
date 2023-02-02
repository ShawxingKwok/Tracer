//@file:Suppress("UNCHECKED_CAST")
//
//package com.example.syntax
//
//public interface I<T> : MutableList<T>{
//    public fun getIt(): T
//    public fun setIt(t: T)
//}
//
//public interface Impl<in T: CharSequence> : I<@UnsafeVariance T>{
//    public fun get(): @UnsafeVariance T
//    public fun set(t: T)
//}
//
//public lateinit var impl: Impl<*>
//
//@Suppress("UNCHECKED_CAST")
//public val i: MutableList<CharSequence> = impl as MutableList<CharSequence>
//public val ii: I<CharSequence> = impl as I<CharSequence>
//
//public fun main() {
////    impl.add("")
//
////    impl.get() // CharSequence
////    impl.set() // Nothing
////
////    impl.getIt()
////    impl.setIt("")
////
//    val myClass = MyClass<String>() as MyClass<*>
//    myClass.set()
//}
//
//class MyClass <in T: CharSequence>{
//    fun set(t: T){}
//}