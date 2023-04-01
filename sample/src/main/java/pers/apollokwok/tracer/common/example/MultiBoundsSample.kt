package pers.apollokwok.tracer.common.example

import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.MultiBoundsSampleTracer
import java.io.Serializable

interface MyList<T> : List<T>
    where T: Serializable, T: CharSequence

interface A
interface B
interface L<T>
//val myList: MyList<*> = TODO()

@Tracer.Root
abstract class MultiBoundsSample<T, V: MutableList<T>> : List<T>, L<V>
    where T: A, T: B
{
    lateinit var t: T
    val j: J<T> = TODO()
}

lateinit var multiBoundsSample: MultiBoundsSample<*, *>
//val list: MutableList<out B> = multiBoundsSample.list as MutableList<out B>

interface J<T> : K<T>{
    val t: T
}
interface K<T>

fun main() {
    multiBoundsSample.j.t as B
}

//internal val MultiBoundsSampleTracer.`_T_‹A，B›_MultiBoundsSample_t` inline get() = `_MultiBoundsSample`.`t`
//internal val MultiBoundsSampleTracer.`_A_MultiBoundsSample_t` inline get() = `_MultiBoundsSample`.`t` as A
//internal val MultiBoundsSampleTracer.`_B_MultiBoundsSample_t` inline get() = `_MultiBoundsSample`.`t` as B

//internal val MultiBoundsSampleTracer.`_J‹↓T_‹A，B››_MultiBoundsSample_list` inline get() = `_MultiBoundsSample`.j
//internal val MultiBoundsSampleTracer.`_K‹↓T_‹A，B›¦›_MultiBoundsSample_list` inline get() = `_MultiBoundsSample`.j as K<*>