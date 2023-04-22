package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.resolver

internal val KSClassDeclaration.tracerInterfaces: Pair<KSClassDeclaration, KSClassDeclaration> inline get(){
    val (name, _name) = getInterfaceNames(this)
    val path = Path(this, name)
    val _path = Path(this, _name)
    val klass = resolver.getClassDeclarationByName(path.toString())!!
    val _klass = resolver.getClassDeclarationByName(_path.toString())!!
    return klass to _klass
}