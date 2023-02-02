package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.noPackageName
import pers.apollokwok.ksputil.qualifiedName

public fun KSClassDeclaration.starTypeContent(imports: List<String>): String =
    buildString {
        if (topParentDecl.qualifiedName() in imports)
            append(noPackageName())
        else
            append(qualifiedName())

        if (typeParameters.any()){
            append("<")
            append(typeParameters.joinToString(", "){ "*" })
            append(">")
        }
    }