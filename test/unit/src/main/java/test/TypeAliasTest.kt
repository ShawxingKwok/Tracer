package test

interface DA<E, F, G> : MutableMap<E, Map<F, G>>

internal typealias X<A> = Y<A, Int>
public typealias Y<A, B> = Z<A?, String, B>
private typealias Z<A, B, C> = DA<A, B, C>?

class TypeTest<T: CharSequence, A, B>: DA<T, A, B>{
    lateinit var c: DA<T, A, B>

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun containsKey(key: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsValue(value: Map<A, B>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: T): Map<A, B>? {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override val entries: MutableSet<MutableMap.MutableEntry<T, Map<A, B>>>
        get() = TODO("Not yet implemented")
    override val keys: MutableSet<T>
        get() = TODO("Not yet implemented")
    override val values: MutableCollection<Map<A, B>>
        get() = TODO("Not yet implemented")

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun put(key: T, value: Map<A, B>): Map<A, B>? {
        TODO("Not yet implemented")
    }

    override fun putAll(from: Map<out T, Map<A, B>>) {
        TODO("Not yet implemented")
    }

    override fun remove(key: T): Map<A, B>? {
        TODO("Not yet implemented")
    }
}

lateinit var typeTest: TypeTest<String, Long, Int>