//package com.example.syntax
//
//interface Animal
//interface Human : Animal
//interface Child : Human
//
//interface J<T: Human, V: Animal, K>{
//    fun getT(): T
//    fun setT(t: T)
//
//    fun getV(): V
//    fun setV(v: V)
//
//    fun getK(): K
//    fun setK(t: K)
//}
//
//typealias MyJ<X, Y> = J<X, in Y, MutableList<Y>>
////lateinit var myJ: MyJ<Child, Human>
////val _myJ: J<Child, in Human, MutableList<Human>> = myJ
//
//lateinit var anotherMyJ: J<Child, *, MutableList<out Animal>>
//
//val f : MyJ<Child, out Animal> = anotherMyJ
//
//// 失效
//@Suppress("UNCHECKED_CAST")
//val _anotherMyJ: J<Child, *, MutableList<out Animal>> = anotherMyJ as J<Child, *, MutableList<out Animal>>
//
//fun main() {
////    myJ.getK() // MutableList<Human>
////    myJ.setK() // MutableList<Human>
//
////    anotherMyJ.getK() // MutableList<*>
////    anotherMyJ.setK() // MutableList<*>
//
//    f.getK()
//    f.setK()
//}