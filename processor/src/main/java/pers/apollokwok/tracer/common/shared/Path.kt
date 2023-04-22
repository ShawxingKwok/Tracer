package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.packageName
import pers.apollokwok.ktutil.updateIf

public class Path(
    private val packageName: String,
    private val name: String,
) {
    public constructor(srcKlass: KSClassDeclaration, name: String) :
            this(srcKlass.packageName(), name)

    override fun toString(): String = packageName.updateIf({ it.any() }){ "$it." } + name
}