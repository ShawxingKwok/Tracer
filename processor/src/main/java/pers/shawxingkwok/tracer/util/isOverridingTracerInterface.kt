package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import pers.shawxingkwok.ksputil.Log
import pers.shawxingkwok.ksputil.qualifiedName
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.tracer.TracerGeneration
import pers.shawxingkwok.tracer.shared.Tags

private val cache = mutableMapOf<String, Boolean>()

internal fun KSPropertyDeclaration.isOverridingTracerInterface(): Boolean =
    cache.getOrPut(qualifiedName()!!) {
        require(Tags.interfacesBuilt)

        parentDeclaration as KSClassDeclaration? ?: return@getOrPut false
        if (!simpleName().startsWith("_")) return@getOrPut false

        var p = this
        while (true){
            p = p.findOverridee() ?: return@getOrPut false
            if (p.parentDeclaration!!.isAnnotationPresent(TracerGeneration.Interface::class))
                return@getOrPut true
        }
        @Suppress("UNREACHABLE_CODE")
        error("Unreachable!")
    }