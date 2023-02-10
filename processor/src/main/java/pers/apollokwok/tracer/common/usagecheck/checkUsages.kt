package pers.apollokwok.tracer.common.usagecheck

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
import pers.apollokwok.tracer.common.util.*
import pers.apollokwok.tracer.common.util.filterOutRepeated
import pers.apollokwok.tracer.common.util.insideModuleVisibleKlasses
import pers.apollokwok.tracer.common.util.isAnnotatedRootOrNodes
import pers.apollokwok.tracer.common.util.isNative
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

private fun forbidPersonalUsageOfTracerPackage(){
    requireNone(resolver.getAllFiles().filter { it.packageName() == Names.GENERATED_PACKAGE }.toList()){
        "Package `${Names.GENERATED_PACKAGE}` can't be used personally."
    }
}

private fun forbidPersonalUsageOfTracerInterface(){
    val forbiddenAnnotNames = listOf(TracerInterface::class).map { it.qualifiedName!! }
    requireNone(symbols = forbiddenAnnotNames.flatMap { resolver.getSymbolsWithAnnotation(it) }){
        "$forbiddenAnnotNames can be used only in generated tracer files, so don't use it yourself."
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
        "Each context class of below classes annotated with ${Names.Nodes}, " +
            "should be annotated with ${Names.Root} or ${Names.Nodes}."
    }
}

private var called = AtomicBoolean(false)
internal fun checkUsages(): Boolean {
    require(!called.getAndSet(true))
    requireWholeRebuildingEveryTime()
    forbidRepeatedNativeContractedNames()
    forbidSameFileNames()
    forbidPersonalUsageOfTracerPackage()
    forbidPersonalUsageOfTracerInterface()
    forbidJavaFileUsingTracerAnnot()
    requireAllRootNodesTipsVisible()
    requireRootNodesTipsSinglyUsed()
    checkNodesContexts()
    return valid
}