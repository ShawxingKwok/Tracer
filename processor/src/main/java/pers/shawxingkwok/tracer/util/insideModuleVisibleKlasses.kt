package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

internal fun List<KSDeclaration>.insideModuleVisibleKlasses(): List<KSClassDeclaration> =
    filterIsInstance<KSClassDeclaration>()
    .filter {
        it.classKind != ClassKind.ENUM_ENTRY
        && it.moduleVisibility() != null
    }
    .flatMap {
        listOf(it) + it.declarations.toList().insideModuleVisibleKlasses()
    }