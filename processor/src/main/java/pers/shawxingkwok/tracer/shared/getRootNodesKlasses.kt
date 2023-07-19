package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.getAnnotatedSymbols
import pers.apollokwok.ksputil.resolver
import pers.apollokwok.tracer.common.annotations.Tracer

public fun getRootNodesKlasses(): List<KSClassDeclaration> =
    resolver.getAnnotatedSymbols<Tracer.Root, KSClassDeclaration>() +
    resolver.getAnnotatedSymbols<Tracer.Nodes, KSClassDeclaration>()