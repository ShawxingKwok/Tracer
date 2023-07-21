package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.tracer.Tracer
import kotlin.contracts.contract

private val cache = mutableMapOf<String, Boolean>()

internal fun KSDeclaration.isAnnotatedRootOrNodes(): Boolean {
     contract {
          returns(true) implies (this@KSDeclaration is KSClassDeclaration)
     }

     return cache.getOrPut(qualifiedName() ?: return false ) {
          isAnnotationPresent(Tracer.Root::class) || isAnnotationPresent(Tracer.Nodes::class)
     }
}