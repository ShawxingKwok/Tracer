package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.resolver
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.toProto

private val cache = mutableMapOf<KSClassDeclaration, Type.Specific>()

internal val KSClassDeclaration.starType: Type.Specific inline get() =
    cache.getOrPut(this){
        resolver.createKSTypeReferenceFromKSType(asStarProjectedType())
        .toProto()
        .convertAll(emptyMap()) as Type.Specific
    }