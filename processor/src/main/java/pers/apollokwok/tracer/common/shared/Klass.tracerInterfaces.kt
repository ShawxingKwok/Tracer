package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.resolver

internal val KSClassDeclaration.tracerInterfaces: Pair<KSClassDeclaration, KSClassDeclaration> inline get() =
    getInterfaceNames(this)
        .toList()
        .map { resolver.getClassDeclarationByName("$tracePackageName.$it")!! }
        .run { get(0) to get(1) }