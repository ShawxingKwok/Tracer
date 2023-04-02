package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal sealed class Convertible<T: Convertible<T>>{
    // The returned latter 'Boolean' means whether 'outer out' is required or not.
    abstract fun convertGeneric(map: Map<String, Arg<*>>, fromAlias: Boolean = false): Pair<T, Boolean>
    abstract fun convertAlias(): T
    abstract fun convertStar(): T

    fun convertAll(map: Map<String, Arg<*>>): T = convertAlias().convertStar().convertGeneric(map).first

    abstract val allInnerKlasses: List<KSClassDeclaration>
    abstract fun getContent(getPathImported: (KSClassDeclaration) -> Boolean): String
    abstract fun getName(isGross: Boolean): String
}