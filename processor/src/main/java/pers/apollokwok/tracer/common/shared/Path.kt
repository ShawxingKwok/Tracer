package pers.apollokwok.tracer.common.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.packageName
import pers.apollokwok.ktutil.updateIf

public class Path(
    private val packageName: String,
    private val klassName: String,
) {
    public constructor(srcKlass: KSClassDeclaration, newKlassName: String) :
            this(srcKlass.packageName(), newKlassName)

    override fun toString(): String = packageName.updateIf({ it.any() }){ "$it." } + klassName
}