package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.Imports

internal sealed class Convertible<T: Convertible<T>>{
    // The returned latter 'Boolean' means whether 'outer out' is required or not.
    abstract fun convertGeneric(map: Map<String, Arg<*>>, isFromAlias: Boolean = false): Pair<T, Boolean>
    abstract fun convertAlias(): T
    abstract fun convertStar(): T

    fun convertAll(map: Map<String, Arg<*>>): T = convertAlias().convertStar().convertGeneric(map).first

    abstract val allInnerKSClasses: List<KSClassDeclaration>
    abstract fun getContent(imports: Imports): String
    abstract fun getName(isGross: Boolean): String
}