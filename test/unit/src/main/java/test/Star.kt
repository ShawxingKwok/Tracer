package test

interface FAG<T: Human, V: Map<List<T>, Any>, K: T>
lateinit var fag: FAG<in Child, *, *>

//var faf: FAG<in Child, out Map<out List<Human>, Any>, out Human> = fag