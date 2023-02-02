package test

import kotlinx.coroutines.Job
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.FixedTypePartTracer

@Tracer.Root
class FixedTypePart<T> : List<T>, FixedTypePartTracer
    where T: CharSequence, T: Organ
{
    var list: MutableMap<List<String?>, kotlin.String?>? = null
    var _list: MutableMap<List<T>, T?>? = null

    lateinit var gs: GS<*, T>
    lateinit var _gs: GS<*,*>
    var t: T = TODO()

    lateinit var kjob: Job
    lateinit var libJob: com.example.testlib.Job

    var func: ((test.String, Long)->Unit)? = TODO()
    val suspendFunc: suspend (test.String, Long)->Unit = TODO()

    val str = String()

    val ts: List<T> = TODO()

    lateinit var organ: Organ

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

    override val _FixedTypePart: FixedTypePart<*> get() = this
}

class String{
    val i = 1
    val gsg = GSGAlias()
    lateinit var organ: Organ
}

public typealias GSGAlias = GSG

class GSG{
    internal val gs: GS<*,*> = TODO()
}

interface GS<T, V> : Iterable<T>
    where T: List<*>, T: CharSequence