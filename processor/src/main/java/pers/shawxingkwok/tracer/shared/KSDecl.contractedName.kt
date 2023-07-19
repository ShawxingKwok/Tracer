package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSDeclaration
import pers.apollokwok.ksputil.noPackageName

public val KSDeclaration.contractedName: String get() = noPackageName()!!.replace(".", "")
public val KSDeclaration.contractedUnderlineName: String get() = noPackageName()!!.replace(".", "_")
public val KSDeclaration.contractedFakeDotName: String get() = noPackageName()!!.replace(".", "․")