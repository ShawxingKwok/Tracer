package test

interface SuperTypesPassingTest<A, B: MutableMap<Human, out A>> : AAF<B, A>, BBG<B, A>

interface AAF<C: Map<Human, D>, D> : List<C>,
    MyMap<D, C>
//    MutableMap<D, MutableSet<C>>

interface BBG<T, V> : List<T>

typealias MyMap<X, Y> = MutableMap<X, MutableSet<Y>>

lateinit var superTypesPassingTest: SuperTypesPassingTest<*, *>
val aff = superTypesPassingTest
val aff1: AAF<out MutableMap<Human, out Any?>, out Any?> = aff
val aff2: List<MutableMap<Human, out Any?>> = aff
val aff3: Collection<MutableMap<Human, out Any?>> = aff
val aff4: Iterable<MutableMap<Human, out Any?>>  = aff
val aff5: MutableMap<out Any?, out MutableSet<out MutableMap<Human, out Any?>>> = aff
val aff6: Map<out Any?, MutableSet<out MutableMap<Human, out Any?>>> = aff
val aff7: BBG<out MutableMap<Human, out Any?>, out Any?> = aff