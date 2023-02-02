package test

import com.example.testlib.Nature
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.MyNatureTracer

@Tracer.Root
open class MyNature : Nature(), MyNatureTracer {
    override val _MyNature: MyNature get() = this

    override val earth: MyEarth = MyEarth(this)

//    abstract val abstractX: Int
    open val openSWithField = "D"

//    @Tracer.Declare
    open val openNoField get() = "D"

    val lazyProp by lazy { 2 }

    val fa = "D"
    val sfa get() = "D"

    @Tracer.Declare
    val declare get() = "D"

    val declareNo = "fgfgj"
}