import java.io.File

interface GSF<T> : MutableList<T>
        where T: File,
              T: CharSequence

internal class GenericContainer<T> {
    lateinit var gsf: Map<String, GSF<*>>

    lateinit var lambda: suspend (String, Long) -> Unit
    var list: Collection<T>? = null
//    init {
//        kotlin.collections.Collection
//    }
}