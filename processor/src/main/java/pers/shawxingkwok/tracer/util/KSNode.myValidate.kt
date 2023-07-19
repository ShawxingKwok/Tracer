package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import pers.apollokwok.ksputil.KSDefaultValidator
import pers.apollokwok.ksputil.alsoRegister

private val cache = mutableMapOf<KSNode, Boolean?>().alsoRegister()

// null means there are some new unsupported syntax
internal fun KSNode.myValidate(): Boolean? =
    cache.getOrPut(this) {
        try {
            accept(MyValidator, Unit)
        } catch (e: Exception) {
            null
        }
    }

private object MyValidator : KSDefaultValidator(){
    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit): Boolean {
        typeReference.checkTAny()
        return super.visitTypeReference(typeReference, data)
    }

    // throw exception if 'T & Any' appears.
    private fun KSTypeReference.checkTAny() {
        element?.typeArguments?.forEach { it.type?.checkTAny() }
    }
}