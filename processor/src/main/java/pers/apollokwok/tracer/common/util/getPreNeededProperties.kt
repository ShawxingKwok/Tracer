package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ksputil.getAnnotationByType
import pers.apollokwok.tracer.common.annotations.Tracer

private val cache = mutableMapOf<KSClassDeclaration, List<KSPropertyDeclaration>>().alsoRegister()

// todo: add one condition: propDecl.contextReceiver == null
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
                    prop.getAnnotationByType<Tracer.Declare>()
                        ?.enabled
                        ?: (prop.moduleVisibility() != null
                            && prop.extensionReceiver == null
                            && (prop.isMyAbstract() || prop.hasBackingField || prop.isDelegated())
                            && !prop.isOverridingTracerInterface()
                        )
                }
                .toList()

            else -> emptyList()
        }
    }