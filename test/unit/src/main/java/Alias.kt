interface InOutPair<in A, out B>{
    val b: B
}
typealias MyPair<X, Y> = InOutPair<List<X>, Map<X, Y>>
typealias MyDoublePair<A, B, C> = MyPair<MyPair<A, B>, Out<In<C?>>>

var myDoublePair: MyDoublePair<out Human, List<CharSequence>, String>? = null
val _myDoublePair: InOutPair<List<InOutPair<List<Human>, Map<out Human, List<CharSequence>>>>, Map<InOutPair<List<Human>, Map<out Human, List<CharSequence>>>, Out<In<String?>>>>? =
    myDoublePair

val lambda: ((Int)->Unit) = TODO()
val parenthesis = lambda
//                 InOutPair<List<InOutPair<List<Human>, Map<out Human, List<CharSequence>>>>, Map<InOutPair<List<Human>, Map<out Human, List<CharSequence>>>, Out<In<String?>>>>?