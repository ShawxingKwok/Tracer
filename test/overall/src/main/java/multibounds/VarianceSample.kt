package multibounds

import pers.shawxingkwok.tracer.Tracer
import java.io.Serializable

@Tracer.Root
class VarianceSample {
    class Foo<in T: Serializable, V: Serializable?>

    val foo: Foo<*, *> = TODO()
}