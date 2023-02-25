package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference

private val cache = mutableMapOf<KSType, Type<*>>()

internal fun KSTypeReference.toProtoWithoutAliasAndStar(): Type<*> =
    cache.getOrPut(resolve()){
        toProto().convertAlias().convertStar()
    }