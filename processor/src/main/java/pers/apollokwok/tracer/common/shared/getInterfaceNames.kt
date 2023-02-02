package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration

public fun getInterfaceNames(klass: KSClassDeclaration): Pair<String, String> {
    val interfaceName = "${klass.contractedName}${Names.Tracer}"
    val outerInterfaceName = "${Names.OUTER}$interfaceName"
    return interfaceName to outerInterfaceName
}