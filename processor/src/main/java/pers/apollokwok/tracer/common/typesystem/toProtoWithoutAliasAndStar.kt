package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.symbol.KSTypeReference

private val cache = Cache.Type()

internal fun KSTypeReference.toProtoWithoutAliasAndStar(): Type<*> =
    cache.getOrPut(resolve()){
        toProto().convertAlias().convertStar()
    }