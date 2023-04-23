package pers.apollokwok.tracer.common.interfacehandler

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import pers.apollokwok.tracer.common.shared.Path
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
    Tags.interfacesBuilt = true
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

    val (name, superName, contextName, grandpaContextName) =
        listOf(
            klass,
            // this property is already overridden.
            superRootOrNodeKlass?.takeUnless { it.isMyOpen() },
            context,
            context?.context,
        )
        .map { it?.let(::getRootNodesName) }

    val imports = Imports(
        srcDecl = klass,
        klasses = listOfNotNull(
            klass,
            superRootOrNodeKlass?.takeUnless { it.isMyOpen() },
            context?.context
        ),
        TracerInterface::class
    )

    val (type, superType, grandpaContextType) =
        listOf(klass, superRootOrNodeKlass?.takeUnless { it.isMyOpen() }, context?.context)
        .map {
            it ?: return@map null
            buildString {
                imports.getName(it).let(::append)
                if (it.typeParameters.any()){
                    append("<")
                    append(it.typeParameters.joinToString(", ") { "*" })
                    append(">")
                }
            }
        }

    val interfacePaths = mutableListOf<Path>()

    if (superRootOrNodeKlass != null
        && superRootOrNodeKlass.packageName() != klass.packageName()
    )
        interfacePaths += getInterfaceNames(superRootOrNodeKlass)
            .toList()
            .map { Path(superRootOrNodeKlass, it) }

    if (context != null && context.packageName() != klass.packageName())
        interfacePaths += Path(context, getInterfaceNames(context).second)

    val content =
        """
        |$SUPPRESSING
        |
        |${if (klass.packageName().any()) "package ${klass.packageName()}" else "" }
        |
        |$imports
        |${interfacePaths.joinToString("\n"){ "import $it" }}
        |
        |@${Names.TracerInterface}
        |$visibilityPart interface $interfaceName$implementsPart{
        |    val `_$name`: $type 
        |    override val `_$superName`: $superType get() = `_$name`
        |    override val `__$grandpaContextName`: $grandpaContextType get() = `__$contextName`.`__$grandpaContextName` 
        |}
        |
        |@${Names.TracerInterface}
        |$visibilityPart interface $outerInterfaceName$outerImplementsPart{
        |    val `__$name`: $type
        |    override val `__$superName`: $superType get() = `__$name`
        |    override val `__$grandpaContextName`: $grandpaContextType get() = `__$contextName`.`__$grandpaContextName` 
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
        fileName = klass.noPackageName() + Names.Tracer + "s",
        // todo: change when old generations can be oriented when it is supported.
        dependencies = Dependencies.ALL_FILES,
//        dependencies = Dependencies(false, klass.containingFile!!),
        content = content,
    )
}