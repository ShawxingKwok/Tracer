package example

import pers.apollokwok.tracer.common.annotations.Tracer

@Tracer.Root
class VarSample : VarSampleTracer {
    var x = 1

//    val foo = Foo()

    override val _VarSample: VarSample
        get() = this
}

//context (VarSampleTracer)
//class Foo{
//    private var x: Int
//        get() = _Int_VarSample_x
//        set(value) { _Int_VarSample_x = value }
//
////    private var _x: Int by this@VarSampleTracer::_Int_VarSample_x
//
//    init {
//        x = 2
//    }
//}

fun main() {
    println(VarSample().x)
}