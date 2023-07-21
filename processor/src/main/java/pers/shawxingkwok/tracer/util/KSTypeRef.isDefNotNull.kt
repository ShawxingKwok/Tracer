package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.symbol.KSDefNonNullReference
import com.google.devtools.ksp.symbol.KSTypeReference

internal fun KSTypeReference.isDefNotNull() = element is KSDefNonNullReference