package pers.apollokwok.tracer.common.interfacehandler

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import pers.apollokwok.tracer.common.util.Path
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.ktutil.Bug
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.apollokwok.tracer.common.shared.*
import pers.apollokwok.tracer.common.shared.Tags.AllInternal
import pers.apollokwok.tracer.common.util.*

private tailrec fun getSuperRootOrNodeKlass(klass: KSClassDeclaration): KSClassDeclaration?{
    val superKlass = klass.superTypes
        .map(KSTypeReference::resolve)
        .filterNot(KSType::isError)
        .map { it.declaration }
        .map {
            when(it){
                is KSClassDeclaration -> it
                is KSTypeAlias -> it.findActualType()
                else -> Bug()
            }
        }
        .firstOrNull { it.classKind == ClassKind.CLASS }

    return when{
        superKlass == null -> null

        // is Root/Nodes and its tracer interface is visible
        superKlass.isAnnotatedRootOrNodes()
        &&(superKlass.isNativeKt()
            || resolver.getClassDeclarationByName(
                name = Path(superKlass, getInterfaceNames(superKlass).first).toString()
            )!!
           .isPublic()
        ) ->
            superKlass

        else -> getSuperRootOrNodeKlass(superKlass)
    }
}

internal fun buildInterfaces(){
    getRootNodesKlasses().forEach(::buildInterface)
}

private fun buildInterface(klass: KSClassDeclaration) {
    val (interfaceName, outerInterfaceName) = getInterfaceNames(klass)

    val visibilityPart =
        if (AllInternal)
            Visibility.INTERNAL.name.lowercase()
        else
            klass.moduleVisibility()!!.name.lowercase()

    // for passing the super context to child classes.
    val superRootOrNodeKlass: KSClassDeclaration? = getSuperRootOrNodeKlass(klass)
    val superTracerNames = superRootOrNodeKlass?.let(::getInterfaceNames)

    val context = klass.context
    val outerContextTracerName = context?.let(::getInterfaceNames)?.second

    val (implementsPart, outerImplementsPart) =
        when{
            superTracerNames != null && outerContextTracerName != null ->
                " : ${superTracerNames.first}, $outerContextTracerName" to
                " : ${superTracerNames.second}, $outerContextTracerName"

            superTracerNames != null ->
                " : ${superTracerNames.first}" to
                " : ${superTracerNames.second}"

            outerContextTracerName != null ->
                " : $outerContextTracerName" to
                " : $outerContextTracerName"

            else -> "" to ""
        }

    val (type, superType, contextType, grandpaContextType) =
        listOf(
            klass,
            // this being overridden property is already overridden.
            superRootOrNodeKlass?.takeUnless { it.isMyOpen() },
            context,
            context?.context,
        )
        .map { it?.starType }

    val (name, superName, contextName, grandpaContextName) =
        listOf(type, superType, contextType, grandpaContextType)
        .map { it?.getName(false) }

    val partialImports = Imports(
        srcDecl = klass,
        klasses = listOfNotNull(type, superType, grandpaContextType).flatMap { it.allInnerKlasses },
        TracerInterface::class,
    )

    val tracerInterfaceImports =
        listOfNotNull(
            if (superRootOrNodeKlass != null && superRootOrNodeKlass.packageName() != klass.packageName())
                Path(superRootOrNodeKlass, superTracerNames!!.first)
            else
                null,

            if (superRootOrNodeKlass != null && superRootOrNodeKlass.packageName() != klass.packageName())
                Path(superRootOrNodeKlass, superTracerNames!!.second)
            else
                null,

            context?.let { Path(it, outerContextTracerName!!) }
        )
        .joinToString("\n"){ "import $it" }

    val (typeContent, superTypeContent, grandpaContextTypeContent) =
        listOf(type, superType, grandpaContextType).map { it?.getContent(partialImports) }

    val content =
        """
        |$SUPPRESSING
        |
        |${if (klass.packageName().any()) "package ${klass.packageName()}" else "" }
        |
        |$tracerInterfaceImports
        |$partialImports
        |
        |@${Names.TracerInterface}
        |$visibilityPart interface $interfaceName$implementsPart{
        |    val `_$name`: $typeContent
        |    override val `_$superName`: $superTypeContent get() = `_$name`
        |    override val `__$grandpaContextName`: $grandpaContextTypeContent get() = `__$contextName`.`__$grandpaContextName` 
        |}
        |
        |@${Names.TracerInterface}
        |$visibilityPart interface $outerInterfaceName$outerImplementsPart{
        |    val `__$name`: $typeContent
        |    override val `__$superName`: $superTypeContent get() = `__$name`
        |    override val `__$grandpaContextName`: $grandpaContextTypeContent get() = `__$contextName`.`__$grandpaContextName` 
        |}
        """
        .trimMarginAndRepeatedBlankLines()
        .lines()
        .filterNot { line ->
            line.startsWith("    override val `_null`")
            || line.startsWith("    override val `__null`")
        }
        .joinToString("\n")

    Environment.codeGenerator.createFile(
        packageName = klass.packageName(),
        fileName = klass.noPackageName() + "Tracers",
        // todo: change when old generations can be oriented when it is supported.
        dependencies = Dependencies.ALL_FILES,
//        dependencies = Dependencies(false, klass.containingFile!!),
        content = content,
    )
}