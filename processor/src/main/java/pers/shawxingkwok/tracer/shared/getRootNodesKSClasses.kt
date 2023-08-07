package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.getAnnotatedSymbols
import pers.shawxingkwok.ksputil.resolver
import pers.shawxingkwok.tracer.Tracer

public fun getRootNodesKSClasses(): List<KSClassDeclaration> =
    resolver.getAnnotatedSymbols<Tracer.Root, KSClassDeclaration>() +
    resolver.getAnnotatedSymbols<Tracer.Nodes, KSClassDeclaration>()