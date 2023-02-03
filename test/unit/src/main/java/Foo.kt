annotation class FFoo<T>

@FFoo<List<String>>
val foo = 1

fun main() {
    println(foo)
}