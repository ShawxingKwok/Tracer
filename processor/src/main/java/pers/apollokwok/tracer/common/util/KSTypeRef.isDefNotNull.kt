package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.symbol.KSTypeReference

private val KSDefNonNullReferenceClazz =
    try {
        Class.forName("com.google.devtools.ksp.symbol.KSDefNonNullReference")
    } catch (e: Exception) {
        null
    }

internal fun KSTypeReference.isDefNotNull() =
    KSDefNonNullReferenceClazz?.isInstance(element) == true