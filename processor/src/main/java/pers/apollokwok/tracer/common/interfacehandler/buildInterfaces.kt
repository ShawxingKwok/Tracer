package pers.apollokwok.tracer.common.interfacehandler

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
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
            || "${Names.GENERATED_PACKAGE}.${getInterfaceNames(superKlass).first}"
            .let(resolver::getClassDeclarationByName)!!
            .isPublic()
        ) ->
            superKlass

        else -> getSuperRootOrNodeKlass(superKlass)
    }
}

internal fun buildInterface(klass: KSClassDeclaration) {
    val (interfaceName, outerInterfaceName) = getInterfaceNames(klass)

    val visibilityPart =
        if (AllInternal)
            Visibility.INTERNAL.name.lowercase()
        else
            klass.moduleVisibility()!!.name.lowercase()

    // for passing the super context to child classes.
    val superRootOrNodeKlass: KSClassDeclaration? = getSuperRootOrNodeKlass(klass)
    val superTracerName = superRootOrNodeKlass?.contractedName?.plus(Names.Tracer)

    val context = klass.context
    val contextTracerName = context?.contractedName?.let { "${Names.OUTER}${it}Tracer" }

    val (implementsPart, outerImplementsPart) =
        when{
            superTracerName != null && contextTracerName != null ->
                " : $superTracerName, $contextTracerName" to
                " : ${Names.OUTER}$superTracerName, $contextTracerName"

            superTracerName != null ->
                " : $superTracerName" to
                " : ${Names.OUTER}$superTracerName"

            contextTracerName != null ->
                " : $contextTracerName" to
                " : $contextTracerName"

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

    val imports = Imports(
        listOfNotNull(type, superType, grandpaContextType).flatMap { it.allInnerKlasses },
        TracerInterface::class,
    )

    val (typeContent, superTypeContent, grandpaContextTypeContent) =
        listOf(type, superType, grandpaContextType).map { it?.getContent(imports) }

    val content =
        """
        |$SUPPRESSING
        |
        |package ${Names.GENERATED_PACKAGE}
        |$imports
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
        |
        |$visibilityPart val $interfaceName.`_$outerInterfaceName` inline get() = 
        |   object : $outerInterfaceName{
        |       override val `__$name` = `_$name`
        |       override val `__$contextName` = this@$interfaceName.`__$contextName`  
        |   }
        |   
        |$visibilityPart val $outerInterfaceName.`__$interfaceName` inline get() = 
        |   object : $interfaceName{
        |       override val `_$name` = `__$name`
        |       override val `__$contextName` = this@$outerInterfaceName.`__$contextName`  
        |   }
        """
        .trimMargin()
        .lines()
        .filterNot {
            val text = it.trimStart()
            text.startsWith("override val `_null`")
            || text.startsWith("override val `__null`")
        }
        .joinToString("\n")

    Environment.codeGenerator.createFile(
        packageName = Names.GENERATED_PACKAGE,
        fileName = interfaceName + "s",
        dependencies = Dependencies(false, klass.containingFile!!),
        content = content,
    )
}