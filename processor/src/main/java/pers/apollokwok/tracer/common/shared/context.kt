package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ktutil.getOrPutNullable
import pers.apollokwok.tracer.common.annotations.Tracer

private val cache = mutableMapOf<KSClassDeclaration, KSClassDeclaration?>().alsoRegister()

public val KSClassDeclaration.context: KSClassDeclaration? get() =
    cache.getOrPutNullable(this) {
        annotations.firstOrNull {
            it.shortName.getShortName() == Tracer.Nodes::class.simpleName!!
            && it.annotationType.resolve().declaration.qualifiedName?.asString() == Tracer.Nodes::class.qualifiedName!!
        }
        ?.let {
            (it.arguments.first().value as KSType).declaration as KSClassDeclaration
        }
    }