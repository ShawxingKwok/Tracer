package multibounds

import kotlinx.coroutines.Job
import pers.shawxingkwok.tracer.Tracer

@Tracer.Root
class FixedTypePart<T> : List<T>, FixedTypePartTracer
    where T: CharSequence, T: Organ<*>
{
    var list: MutableMap<List<String?>, kotlin.String?>? = null
    var _list: MutableMap<List<T>, T?>? = null


    lateinit var kjob: Job
    lateinit var libJob: com.example.testlib.Job

    var str = String()

    val ts: List<T> = TODO()

    //region
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun contains(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<T> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun listIterator(): ListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): ListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        TODO("Not yet implemented")
    }
    //endregion

    override val `_FixedTypePart‹↓T-‹CharSequence，Organ‹↓Any？›››`: FixedTypePart<*>
        get() = this
}