package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.tracer.common.annotations.Tracer

private val cache = mutableMapOf<String, Boolean>()

internal fun KSClassDeclaration.isAnnotatedRootOrNodes(): Boolean =
     cache.getOrPut(qualifiedName()!!) {
          isAnnotationPresent(Tracer.Root::class) || isAnnotationPresent(Tracer.Nodes::class)
     }