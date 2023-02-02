package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSDeclaration

public val KSDeclaration.topParentDecl: KSDeclaration get(){
    var topParentDecl = this
    while (true) {
        topParentDecl = topParentDecl.parentDeclaration ?: return topParentDecl
    }
}