package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.ktutil.getOrPutNullable
import pers.shawxingkwok.tracer.Tracer

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