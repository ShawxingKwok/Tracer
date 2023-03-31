import java.io.Serializable

internal interface MyList<in T> : Comparable<T>
    where T: CharSequence, T: Serializable

internal class MultiBounds {
    public val myList: MyList<*> get() = TODO()
}