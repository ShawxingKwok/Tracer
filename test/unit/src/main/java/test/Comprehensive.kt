package test

abstract class Comprehensive<T, K: S, S: MutableMap<out T, Pair<T, Boolean>>> : InOutPair<T, K>
        where T: CharSequence?
//              T: JJ<*,*>

abstract class MyCompound : CharSequence, JJ<Any?, Iterable<Any?>>

abstract class ComprehensiveContainer<A: MyCompound, B: List<C>, C: Map<Long, Set<A>>>
{
    lateinit var comprehensive: Comprehensive<in A, *, *>
}

lateinit var comprehensiveContainer: ComprehensiveContainer<*,*,*>
// [CharSequence?, JJ<*, *>], out Pair<[CharSequence?, JJ<*, *>]
//val comprehensive: Comprehensive<*, out MutableMap<*, Boolean>, out MutableMap<*, Boolean>> = comprehensiveContainer.comprehensive

//val comprehensive = comprehensiveContainer.comprehensive as Comprehensive<*, out MutableMap<out CharSequence?, out Pair<CharSequence?, Boolean>>, MutableMap<*, Pair<*, Boolean>>>
val _comprehensive: InOutPair<*, MutableMap<out CharSequence?, out Pair<CharSequence?, Boolean>>> = comprehensiveContainer.comprehensive