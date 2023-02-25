package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

private val cache = mutableMapOf<Pair<KSType, Boolean>, List<Type.Specific>>()

internal fun KSClassDeclaration.getTraceableSuperRawTypes(isSrc: Boolean): List<Type.Specific> =
    cache.getOrPut(asStarProjectedType() to isSrc){
        if (typeParameters.any())
            getSuperSpecificRawTypes(isSrc).map { it.convertGeneric(convertedStarArgsMap).first }
        else
            getSuperSpecificRawTypes(isSrc)
    }