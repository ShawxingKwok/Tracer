package pers.shawxingkwok.tracer.shared

import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.packageName
import pers.shawxingkwok.ktutil.updateIf

public class Path(
    private val packageName: String,
    private val name: String,
) {
    public constructor(srcKSClass: KSClassDeclaration, name: String) :
            this(srcKSClass.packageName(), name)

    override fun toString(): String = packageName.updateIf({ it.any() }){ "$it." } + name
}