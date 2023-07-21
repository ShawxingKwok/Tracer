package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.symbol.KSNode
import pers.shawxingkwok.ksputil.KSDefaultValidator
import pers.shawxingkwok.ksputil.alsoRegister

private val cache = mutableMapOf<KSNode, Boolean>().alsoRegister()

// TODO(consider undoing cache)
internal fun KSNode.myValidate(): Boolean =
    cache.getOrPut(this) {
        accept(KSDefaultValidator, Unit)
    }