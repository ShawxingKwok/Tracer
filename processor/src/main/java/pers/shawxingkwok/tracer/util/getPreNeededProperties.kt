package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import pers.shawxingkwok.ksputil.alsoRegister
import pers.shawxingkwok.tracer.Tracer

private val cache = mutableMapOf<KSClassDeclaration, List<KSPropertyDeclaration>>().alsoRegister()

internal fun KSClassDeclaration.getPreNeededKSProperties(): List<KSPropertyDeclaration> =
    cache.getOrPut(this) {
        when {
            classKind != ClassKind.CLASS
            || !isNativeKt()
            || isAnnotationPresent(Tracer.Tip::class) -> emptyList()

            // general rebuilt classes
            isAnnotatedRootOrNodes()
            || (getConstructors().singleOrNull()?.let { it.typeParameters + it.parameters }?.none() ?: false
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