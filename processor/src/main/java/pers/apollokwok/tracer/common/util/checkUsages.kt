package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.apollokwok.tracer.common.shared.contractedName
import pers.apollokwok.tracer.common.shared.getRootNodesKlasses
import pers.apollokwok.tracer.common.shared.*
import java.util.concurrent.atomic.AtomicBoolean

private var valid = true

private inline fun requireNone(symbols: List<KSNode>, getMsg: ()->String){
    if (symbols.any()){
        valid = false
        Log.errorLater(msg = getMsg(), symbols = symbols.distinct())
    }
}

// todo: remove when old generations can be oriented when it is supported.
private fun requireWholeRebuildingEveryTime(){
    Environment.codeGenerator.createFile(
        packageName = Names.GENERATED_PACKAGE,
        fileName = "WholeRebuildingRequirement",
        dependencies = Dependencies.ALL_FILES,
        content = "",
        extensionName = ""
    )
}

private fun requireRootNodesUsedOnClasses(){
    requireNone(getRootNodesKlasses().filterNot { it.classKind == ClassKind.CLASS }){
        "Symbols below annotated with ${Names.Root} or ${Names.Nodes} are not classes."
    }
}

// todo: use 'getTrulyAllFiles' when it is supported.
private fun forbidRepeatedNativeContractedNames(){
    requireNone(
        symbols = resolver
            .getAllFiles()
            .flatMap { it.declarations }
            .toList()
            .let { decls ->
                val visibleTypeAliases = decls.filterIsInstance<KSTypeAlias>()
                    .filterNot { it.moduleVisibility() == null }
                visibleTypeAliases + decls.insideModuleVisibleKlasses()
            }
            .filterOutRepeated { it.contractedName }
    ){
        "Names below conflict. To make built names more clear, each internal/public class/typealias " +
            "must be unique for its contracted name in the built module."
    }
}

// todo: use 'getTrulyAnnotatedSymbols' when it is supported.
private fun forbidSameFileNames(){
    requireNone(symbols = getRootNodesKlasses().distinct().filterOutRepeated { it.contractedName.lowercase() }){
        "Rename some classes below which conflict on names in tracer building."
    }
}

private fun forbidJavaFileUsingTracerAnnot(){
    requireNone(
        symbols = Tracer::class.nestedClasses
            .flatMap { resolver.getSymbolsWithAnnotation(it.qualifiedName!!).toList() }
            .filter { it.origin == Origin.JAVA },
    ){
        "${Names.Tracer} annotations can't be used in Java files which are too outdated."
    }
}

private fun requireAllRootNodesTipsVisible(){
    val annotatedKlasses = getRootNodesKlasses() + resolver.getAnnotatedSymbols<Tracer.Tips, KSClassDeclaration>()
    requireNone(annotatedKlasses.filter { it.moduleVisibility() == null }){
        "Each class annotated with ${Names.Root}, ${Names.Nodes}, or ${Names.Tips} must be module-visible."
    }
}

private fun requireRootNodesTipsSinglyUsed(){
    val classes = arrayOf(Tracer.Root::class, Tracer.Nodes::class, Tracer.Tips::class)
    requireNone(getRootNodesKlasses().filter { klass -> classes.count{ klass.isAnnotationPresent(it) } > 1 }){
        "${Names.Root}, ${Names.Nodes} and ${Names.Tips} can't be used together."
    }
}

private fun checkNodesContexts() {
    val (withRightContext, withWrongContext) = resolver
        .getAnnotatedSymbols<Tracer.Nodes, KSClassDeclaration>()
        .partition { it.context!!.isAnnotatedRootOrNodes() }

    withRightContext
        .filter { it.moduleVisibility() == Visibility.PUBLIC }
        .filter {
            val context = it.context!!
            if (context.isNative())
                context.moduleVisibility() == Visibility.INTERNAL
            else {
                val contextTracerInterfacePath = "${Names.GENERATED_PACKAGE}.${getInterfaceNames(context).first}"
                resolver.getClassDeclarationByName(contextTracerInterfacePath)!!.isInternal()
            }
        }
        .let {
            requireNone(it){
                "Visibilities conflict since each public tracer interface of classes below " +
                    "needs to implement the internal tracer interface of its context class."
            }
        }

    requireNone(withWrongContext){
        "Each context class of classes below annotated with ${Names.Nodes}, " +
            "should be annotated with ${Names.Root} or ${Names.Nodes}."
    }
}

private fun checkOmittedSymbols() {
    resolver.getAnnotatedSymbols<Tracer.Omit, KSPropertyDeclaration>()
        .forEach { prop ->
            val reasons = mapOf(
                (prop.parentDeclaration == null) to "top-level",

                ((prop.parentDeclaration as? KSClassDeclaration)?.classKind == ClassKind.INTERFACE) to
                        "in interfaces",

                (prop.moduleVisibility() == null) to "module-invisible",

                (prop.extensionReceiver != null) to "with extensional receiver",
            )
            .filterKeys { it }
            .values

            if (reasons.any())
                Log.w("Property $prop is $reasons and always omitted in tracer building, " +
                   "which means annotating ${Names.Omit} on it makes no sense.")
        }
}

private var called = AtomicBoolean(false)

internal fun checkUsages(): Boolean {
    require(!called.getAndSet(true))

    requireWholeRebuildingEveryTime()
    requireRootNodesUsedOnClasses()
    forbidRepeatedNativeContractedNames()
    forbidSameFileNames()
    forbidJavaFileUsingTracerAnnot()
    requireAllRootNodesTipsVisible()
    requireRootNodesTipsSinglyUsed()
    checkNodesContexts()
    checkOmittedSymbols()

    return valid
}