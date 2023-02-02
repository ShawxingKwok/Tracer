package test

private interface J<A, B, C>: MutableList<A>, Map<B, C>
abstract class AA<X, Y, Z> : J<List<X>, Map<X, Y>, Z>

var aa: AA<in List<CharSequence>, MutableList<Human?>, in Child?>? = null

//val _aa: Map<Map<in List<CharSequence>, MutableList<Human?>>, Child?> = aa

interface MultiBound<T, V: T, K: Map<T, V>>
    where T: List<CharSequence>,
          T: CharSequence
{
    var t: Triple<T, List<V>, K>
}

//Triple<
//  out T_[List<CharSequence>, CharSequence],
//    List<out V_[List<CharSequence>, CharSequence]>,
//    out K_[Map<out T_[List<CharSequence>, CharSequence], out V_[List<CharSequence>, CharSequence]>]>

//lateinit var multiBound: MultiBound<*, Long, *>

//lateinit var multiBound: MultiBound<*>
//
//interface Fooo<T: CharSequence>
//interface Barr<T: MultiBound<*>>

typealias FA<T> = List<In<T>>
lateinit var fa: FA<*>

interface JJ<A, V: Iterable<A>>

interface KK<T, V: Quadruple<T, K, Map<T, *>, *>, K>

// * 背后的 type 为 Quadruple<T, K, Map<K, *>, *>
// T to [in Human], K to [MyLimitedDouble<Child, out Child>]

// "1"
lateinit var kk: KK<in Human, *, MyLimitedDouble<Child, *>>

//var ffafa:
//    KK<
//        in Human,
//        out Quadruple<
//            in Human,
//            MyLimitedDouble<Child, *>,
//            Map<in Human, out Any?>,
//            out Map<MyLimitedDouble<Child, *>, Map<in Human, out Any?>>
//        >,
//        MyLimitedDouble<Child, out Child>
//    >
//= kk
//
//val _kk:
//    KK<
//        in Human,
//        out Quadruple<
//            Human,
//            MyLimitedDouble<Child, out Child>,
//            Map<Human, out Any?>,
//            out Map<MyLimitedDouble<Child, out Child>, Map<Human, out Any?>>
//        >,
//        MyLimitedDouble<Child, out Child>
//    > = kk
// KK<in Human, out Map<in Human, JJ<in Human, *>>



//lateinit var kkContainer: KKContainer<in String?, *, *>

// KKContainer<
//     in String,
//     out Map<in String, Pair<Any?, Boolean>>,
//     out Map<in String, Pair<Any?, Boolean>>>

//val triple: Triple<out CharSequence, out Map<in CharSequence, Pair<in CharSequence, Boolean>>, Human> = kkContainer.triple

interface MyLimitedDouble<S: Human, G: Child>

interface Quadruple<S, G, L, K: Map<G, L>>

interface FGS<T, K: Map<in T, Any>>
lateinit var fgs: FGS<out String, *>
public var _fgs: FGS<out String, out Map<*, Any>> = fgs

//interface FGNOD<T, K: T>
//
//lateinit var fgnod: FGNOD<in String, *>
//val _fgnod: FGNOD<in String, out String> = fgnod

//val kkContainer: KKContainer<*, out List<Map<Long, Set<FGNIOA>>>, *> = comprehensiveTest.kkContainer

//val kkContainer: KKContainer<*, out List<Map<Long, Set<FGNIOA>>>, out Map<*, Pair<CharSequence?, Boolean>>> = comprehensiveTest.kkContainer

// KKContainer<
//      in A_FGNIOA,
//      out B_List<
//          Map<
//              Long,
//              Set<FGNIOA>
//          >
//      >,
//      out Map<
//          in A_FGNIOA,
//          Pair<Any?, Boolean>
//      >
// >

//KKContainer<in A_FGNIOA, out B_List<Map<Long, Set<FGNIOA>>>, out Map<in A_FGNIOA, Pair<Any?, Boolean>>>
//KKContainer<*, out B_List<Map<Long, Set<FGNIOA>>>, out Map<*, Pair<[CharSequence?, JJ<*, out Iterable<Any?>>], Boolean>>>

abstract class RecycleTest<T: RecycleTest<T>> : List<T>

class FOS<T: RecycleTest<T>>
lateinit var fos: FOS<*>
lateinit var _fos: FOS<out RecycleTest<*>>

lateinit var recycleTest: RecycleTest<*>
val _recycleTest: RecycleTest<out RecycleTest<*>> = recycleTest
val list: List<RecycleTest<*>> = recycleTest

interface KFA<A, B: List<A>>
interface KFB<A, B: Map<A, CharSequence>>

interface KFImpl<T> : List<T>, MyCollection<T>, Collection<T>

interface MyCollection<T> : Collection<T>

typealias FNSOG<A, B> = Pair<KFA<A,B>, KFB<A,B>>

//lateinit var fnsog: FNSOG<CharSequence, KFImpl>

interface GNIO<T: Human, in K: In<T>> : MutableMap<@UnsafeVariance T, @UnsafeVariance K>

lateinit var gnio: GNIO<*, *>
val _gnio: MutableMap<out Human, out In<*>> = gnio

//interface FGGS<in T: Human> 代表其内可能有 Human 到 T 之间的元素
//interface FGGS<out T: Human> 代表其内可能有 T 的子类
