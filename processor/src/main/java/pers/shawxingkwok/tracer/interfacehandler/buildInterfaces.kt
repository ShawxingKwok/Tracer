package pers.shawxingkwok.tracer.interfacehandler

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.tracer.TracerGeneration
import pers.shawxingkwok.tracer.shared.Tags.AllInternal
import pers.shawxingkwok.tracer.util.isNativeKt
import pers.shawxingkwok.tracer.shared.*
import pers.shawxingkwok.tracer.util.SUPPRESSING
import pers.shawxingkwok.tracer.util.isAnnotatedRootOrNodes
import pers.shawxingkwok.tracer.util.isMyOpen
import pers.shawxingkwok.tracer.util.moduleVisibility
import pers.shawxingkwok.tracer.util.trimMarginAndRepeatedBlankLines

private tailrec fun getSuperRootOrNodeKSClass(ksClass: KSClassDeclaration): KSClassDeclaration? {
    val superKSClass = ksClass.superTypes
        .map(KSTypeReference::resolve)
        .filterNot(KSType::isError)
        .map { it.declaration }
        .map {
            when(it){
                is KSClassDeclaration -> it
                is KSTypeAlias -> it.findActualType()
                else -> error("")
            }
        }
        .firstOrNull { it.classKind == ClassKind.CLASS }

    return when{
        superKSClass == null -> null

        // is Root/Nodes and its tracer interface is visible
        superKSClass.isAnnotatedRootOrNodes()
        &&(superKSClass.isNativeKt()
            || resolver.getClassDeclarationByName(
                name = Path(superKSClass, getInterfaceNames(superKSClass).first).toString()
            )!!
           .isPublic()
        ) ->
            superKSClass

        else -> getSuperRootOrNodeKSClass(superKSClass)
    }
}

internal fun buildInterface(ksClass: KSClassDeclaration) {
    val (interfaceName, outerInterfaceName) = getInterfaceNames(ksClass)

    val visibilityPart =
        if (AllInternal)
            Visibility.INTERNAL.name.lowercase()
        else
            ksClass.moduleVisibility()!!.name.lowercase()

    // for passing the super context to child classes.
    val superRootOrNodeKSClass: KSClassDeclaration? = getSuperRootOrNodeKSClass(ksClass)
    val superTracerNames = superRootOrNodeKSClass?.let(::getInterfaceNames)

    val context = ksClass.context
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

    // the property is already overridden in that open class
    val mayTakenSuperRootNodesKSClass = superRootOrNodeKSClass?.takeUnless { it.isMyOpen() }

    val (name, superName, contextName, grandpaContextName) =
        listOf(
            ksClass,
            mayTakenSuperRootNodesKSClass,
            context,
            context?.context,
        )
        .map { it?.let(::getRootNodesPropName) }

    val imports = Imports(
        packageName = ksClass.packageName(),
        ksClasses = listOfNotNull(
            ksClass,
            mayTakenSuperRootNodesKSClass,
            context?.context
        ),
        TracerGeneration.Interface::class,
    )

    val (typeContent, superTypeContent, grandpaContextTypeContent) =
        listOf(ksClass, mayTakenSuperRootNodesKSClass, context?.context)
        .map {
            it ?: return@map null
            buildString {
                imports.getKSClassName(it).let(::append)
                if (it.typeParameters.any()){
                    append("<")
                    append(it.typeParameters.joinToString(", ") { "*" })
                    append(">")
                }
            }
        }

    val interfacePaths = mutableListOf<Path>()

    if (superRootOrNodeKSClass != null
        && superRootOrNodeKSClass.packageName() != ksClass.packageName()
    )
        interfacePaths += getInterfaceNames(superRootOrNodeKSClass)
            .toList()
            .map { Path(superRootOrNodeKSClass, it) }

    if (context != null && context.packageName() != ksClass.packageName())
        interfacePaths += Path(context, getInterfaceNames(context).second)

    val content =
        """
        |$SUPPRESSING
        |
        |${if (ksClass.packageName().any()) "package ${ksClass.packageName()}" else "" }
        |
        |$imports
        |${interfacePaths.joinToString("\n"){ "import $it" }}
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
        packageName = ksClass.packageName(),
        fileName = ksClass.noPackageName() + Names.Tracer + "s",
        // todo: change when old generations can be oriented when it is supported.
        dependencies = Dependencies.ALL_FILES,
//        dependencies = Dependencies(false, ksClass.containingFile!!),
        content = content,
    )
}