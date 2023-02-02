package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSDeclaration
import pers.apollokwok.ksputil.noPackageName

public val KSDeclaration.contractedName: String get() = noPackageName()!!.replace(".", "")