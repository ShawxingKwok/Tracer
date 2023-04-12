package pers.apollokwok.tracer.common.interfacehandler

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.noPackageName
import pers.apollokwok.ksputil.resolver
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.tracer.common.shared.getInterfaceNames
import pers.apollokwok.tracer.common.shared.getRootNodesKlasses
import pers.apollokwok.tracer.common.shared.Names
import pers.apollokwok.tracer.common.util.isMyOpen
import java.io.File

private val cache = mutableMapOf<KSClassDeclaration, Map<String, Pair<KSClassDeclaration, Boolean>>>()

private var called = false

// Override cognominal open properties from different super interfaces.
internal fun fixInterfaces(){
    require(!called)
    called = true

    getRootNodesKlasses()
        .flatMap { getInterfaceNames(it).toList() }
        .map { resolver.getClassDeclarationByName("${Names.GENERATED_PACKAGE}.$it")!! }
        .forEach(::fixInterface)

    cache.clear()
}

// TODO: comment
private fun fixInterface(klass: KSClassDeclaration) {
    if (klass in cache) return

    val superInterfaceKlasses = klass.superTypes.map { it.resolve().declaration as KSClassDeclaration }.toList()
    superInterfaceKlasses.forEach(::fixInterface)

    val map = mutableMapOf<String, Pair<KSClassDeclaration, Boolean>>()
    cache[klass] = map
    superInterfaceKlasses.forEach { map += cache[it]!! }
    klass.getDeclaredProperties().forEach {
        val parent = it.parentDeclaration as KSClassDeclaration
        map[it.simpleName()] = parent to it.isMyOpen()
    }

    if (superInterfaceKlasses.count() == 2){
        val (first, second) = superInterfaceKlasses
        val (firstCache, secondCache) = superInterfaceKlasses.map { cache[it]!! }

        val selfPropNames = klass.getDeclaredProperties().map { it.simpleName() }.toList()
        val insertedLines = firstCache.keys
            .intersect(secondCache.keys)
            .asSequence()
            .filterNot { it in selfPropNames }
            .filterNot { propName ->
                val firstKlass = firstCache[propName]!!.first
                val secondKlass = secondCache[propName]!!.first
                if (firstKlass == secondKlass) return@filterNot true
                val firstType = firstKlass.asStarProjectedType()
                val secondType = secondKlass.asStarProjectedType()
                firstType.isAssignableFrom(secondType) || secondType.isAssignableFrom(firstType)
            }
            .mapNotNull { propName ->
                when{
                    firstCache[propName]!!.second  -> propName to first
                    secondCache[propName]!!.second -> propName to second
                    else -> null
                }
            }
            // Add these new property info to cache.
            .onEach { (propName, _) -> map[propName] = klass to true }
            .map { (propName, superKlass) ->
                "    override val `$propName` get() = super<${superKlass.noPackageName()}>.`$propName`"
            }
            .toList()

        if (insertedLines.none()) return

        val file = klass.containingFile!!.filePath.let(::File)
        val lines = file.readLines().toMutableList()
        val i =
            if (klass == klass.containingFile!!.declarations.first())
                lines.indexOf("}")
            else
                lines.lastIndexOf("}")

        lines.addAll(i, insertedLines)
        file.writeText(lines.joinToString("\n"))
    }
}