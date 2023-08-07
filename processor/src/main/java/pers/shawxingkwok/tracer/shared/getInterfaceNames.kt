package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration

public fun getInterfaceNames(ksClass: KSClassDeclaration): Pair<String, String> {
    val interfaceName = "${ksClass.contractedUnderlineName}${Names.Tracer}"
    val outerInterfaceName = "${Names.OUTER}$interfaceName"
    return interfaceName to outerInterfaceName
}