package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.packageName

// klass may have no package name
internal val KSClassDeclaration.tracePackageName: String inline get() =
    listOfNotNull(packageName().takeIf { it.any() }, "trace").joinToString(".")