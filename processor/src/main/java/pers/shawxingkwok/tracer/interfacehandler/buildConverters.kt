package pers.shawxingkwok.tracer.interfacehandler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.tracer.util.SUPPRESSING
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.util.isAnnotatedRootOrNodes
import pers.shawxingkwok.tracer.shared.getInterfaceNames
import pers.shawxingkwok.tracer.shared.getRootNodesKSClasses
import pers.shawxingkwok.tracer.util.moduleVisibility
import pers.shawxingkwok.tracer.util.trimMarginAndRepeatedBlankLines
import pers.shawxingkwok.tracer.shared.tracerInterfaces

internal fun buildConverters(){
    getRootNodesKSClasses().forEach(::buildConverter)
}

private fun buildConverter(ksClass: KSClassDeclaration){
    val (interfaceName, outerInterfaceName) = getInterfaceNames(ksClass)

    val ksPropPairs = ksClass.tracerInterfaces.toList()
        .map {
            it.getAllProperties()
                .sortedBy { ksProp -> ksProp.type.resolve().declaration.toString() }
                .toList()
        }
        .let { (first, second) -> first.zip(second) }

    val v = if (Tags.AllInternal) "internal" else ksClass.moduleVisibility()!!.name.lowercase()

    val decls = ksPropPairs.joinToString("\n        ") {
        (name, _name) -> "override val `$_name` get() = this@$interfaceName.`$name`"
    }

    val outerDecls = ksPropPairs.joinToString("\n        ") {
        (name, _name) -> "override val `$name` get() = this@$outerInterfaceName.`$_name`"
    }

    val content =
        """
        |$SUPPRESSING
        |
        |${if (ksClass.packageName().any()) "package ${ksClass.packageName()}" else "" }
        |
        |$v val $interfaceName.`_$outerInterfaceName`: $outerInterfaceName inline get() = 
        |    object : $outerInterfaceName{
        |        $decls
        |    }
        |   
        |$v val $outerInterfaceName.`__$interfaceName`: $interfaceName inline get() = 
        |    object : $interfaceName{
        |        $outerDecls
        |    }
        """.trimMarginAndRepeatedBlankLines()

    Environment.codeGenerator.createFile(
        packageName = ksClass.packageName(),
        fileName = ksClass.noPackageName() + "Converters",
        dependencies = Dependencies(
            aggregating = false,
            sources = ksClass
                .getAllSuperTypes()
                .map { it.declaration }
                .filter { it.isAnnotatedRootOrNodes() }
                .mapNotNull { it.containingFile }
                .toList()
                .toTypedArray()
        ),
        content = content
    )
}