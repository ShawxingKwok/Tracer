package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Visibility

internal fun limitVisibility(vararg visibilities: Visibility?): Visibility? {
    var result = Visibility.PUBLIC
    visibilities.forEach { v ->
        when(v){
            Visibility.PUBLIC -> {}
            Visibility.INTERNAL -> result = v
            else -> return null
        }
    }
    return result
}

internal fun limitVisibility(vararg decls: KSDeclaration): Visibility? =
    Array(decls.size){
        decls[it].moduleVisibility()
    }
    .let {
        limitVisibility(*it)
    }