package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.noPackageName
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.tracer.common.shared.outermostDecl

internal fun KSClassDeclaration.starTypeContent(imports: List<String>): String =
    buildString {
        if (imports.any() && outermostDecl.qualifiedName() in imports)
            append(noPackageName())
        else
            append(qualifiedName())

        if (typeParameters.any()){
            append("<")
            append(typeParameters.joinToString(", "){ "*" })
            append(">")
        }
    }