package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSDeclaration

public val KSDeclaration.outermostDecl: KSDeclaration get() =
    this.parentDeclaration?.outermostDecl ?: this