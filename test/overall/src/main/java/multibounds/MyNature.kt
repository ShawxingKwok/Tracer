package multibounds

import com.example.testlib.Earth
import com.example.testlib.Nature
import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
open class MyNature : Nature(), MyNatureTracer {
    override val _MyNature: MyNature get() = this
    override val earth: Earth get() = TODO("Not yet implemented")
    override val _Nature: Nature get() = this

    //    abstract val abstractX: Int
    open val openSWithField = "D"

//    @Tracer.Declare
    open val openNoField get() = "D"

    val lazyProp by lazy { 2 }

    val fa = "D"
    val sfa get() = "D"

    @Tracer.Omit
    val declare get() = "D"

    val declareNo = "fgfgj"
}