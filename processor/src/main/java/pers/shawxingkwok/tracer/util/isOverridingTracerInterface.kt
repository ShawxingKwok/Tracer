package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.shawxingkwok.tracer.shared.Tags

private val cache = mutableMapOf<String, Boolean>()

// can't be used in 1st round to ensure all tracer interfaces have been built.
// findOverridee has bug in ksp 1.7.20-1.0.8
internal fun KSPropertyDeclaration.isOverridingTracerInterface(): Boolean =
    cache.getOrPut(qualifiedName()!!) {
        require(Tags.interfacesBuilt)

        val parentKlass = parentDeclaration as KSClassDeclaration? ?: return@getOrPut false
        if (Modifier.OVERRIDE !in modifiers) return@getOrPut false
        if (type.myValidate() != true) return@getOrPut false

        if (!simpleName().startsWith("_")) return@getOrPut false

        parentKlass.getAllSuperTypes()
            .filter { it.declaration.isAnnotationPresent(TracerInterface::class) }
            .map { it.declaration as KSClassDeclaration }
            .flatMap { it.getDeclaredProperties() }
            .any { it.simpleName() == simpleName() }
    }