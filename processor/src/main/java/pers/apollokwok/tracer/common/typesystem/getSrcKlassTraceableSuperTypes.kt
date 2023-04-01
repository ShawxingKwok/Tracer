package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ktutil.updateIf

internal fun getSrcKlassTraceableSuperTypes(srcKlass: KSClassDeclaration): List<Type.Specific> =
    if (srcKlass.typeParameters.any())
        srcKlass.getSuperSpecificRawTypes(true).map { it.convertGeneric(srcKlass.typeParamBoundsMap).first }
    else
        srcKlass.getSuperSpecificRawTypes(true)