package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Visibility
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.ktutil.getOrPutNullable

private val cache = mutableMapOf<String, Visibility?>()

internal fun KSDeclaration.moduleVisibility(): Visibility? =
    when{
        this.isLocal() -> null

        parentDeclaration == null -> limitVisibility(getVisibility())

        else ->
            cache.getOrPutNullable(qualifiedName()!!) {
                limitVisibility(getVisibility(), parentDeclaration!!.moduleVisibility())
            }
    }