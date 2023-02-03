interface MyHuman<A, B>: InOutPair<MutableList<A>, MutableList<B>>

lateinit var myHuman: MyHuman<out Human, in Child>
val _myHuman: InOutPair<*, MutableList<in Child>> = myHuman