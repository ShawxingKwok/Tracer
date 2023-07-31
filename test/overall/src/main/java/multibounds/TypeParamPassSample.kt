package multibounds

import pers.shawxingkwok.tracer.Tracer
import java.io.Serializable

@Tracer.Root
class TypeParamPassSample {
    class Foo<T: V, V: Serializable?>

    lateinit var foo: Foo<*, *>
}