package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal fun getSrcKlassTraceableSuperTypes(srcKlass: KSClassDeclaration): List<Type.Specific> =
    if (srcKlass.typeParameters.any())
        srcKlass.getSuperSpecificRawTypes(true).map { it.convertGeneric(srcKlass.convertedStarArgsMap).first }
    else
        srcKlass.getSuperSpecificRawTypes(true)