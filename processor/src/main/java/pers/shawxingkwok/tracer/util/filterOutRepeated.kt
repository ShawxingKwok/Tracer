package pers.shawxingkwok.tracer.util

internal inline fun <T> Iterable<T>.filterOutRepeated(predicate: (T) -> Any? = { it }): List<T> =
    groupBy(predicate)
    .values
    .filter { it.size > 1 }
    .flatten()