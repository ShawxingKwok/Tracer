package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.getAnnotatedKlasses
import pers.apollokwok.ksputil.resolver
import pers.apollokwok.tracer.common.annotations.Tracer

public fun getRootNodesKlasses(): List<KSClassDeclaration> =
    resolver.getAnnotatedKlasses<Tracer.Root>() + resolver.getAnnotatedKlasses<Tracer.Nodes>()