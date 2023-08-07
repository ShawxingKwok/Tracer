package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.resolver

internal val KSClassDeclaration.tracerInterfaces: Pair<KSClassDeclaration, KSClassDeclaration> inline get(){
    val (name, _name) = getInterfaceNames(this)
    val path = Path(this, name)
    val _path = Path(this, _name)
    val ksClass = resolver.getClassDeclarationByName(path.toString())!!
    val _ksClass = resolver.getClassDeclarationByName(_path.toString())!!
    return ksClass to _ksClass
}