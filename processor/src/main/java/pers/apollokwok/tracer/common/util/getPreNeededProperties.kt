package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.tracer.common.annotations.Tracer

private val cache = mutableMapOf<KSClassDeclaration, List<KSPropertyDeclaration>>().alsoRegister()

internal fun KSClassDeclaration.getPreNeededProperties(): List<KSPropertyDeclaration> =
    cache.getOrPut(this) {
        when {
            !isNative()
            || Modifier.DATA in modifiers
            || isAnnotationPresent(Tracer.Tips::class) -> emptyList()

            // general rebuilt classes
            isAnnotatedRootOrNodes()
            || (getConstructors().singleOrNull()?.let { it.typeParameters + it.parameters }?.none() == true
                && typeParameters.none()
                && isFinal()
            ) ->
                getDeclaredProperties().filter { prop ->
                    // todo: add one condition: prop.contextReceiver == null
                    !prop.isAnnotationPresent(Tracer.Omit::class)
                    && prop.moduleVisibility() != null
                    && prop.extensionReceiver == null
                    && !prop.isOverridingTracerInterface()
                }
                .toList()

            else -> emptyList()
        }
    }