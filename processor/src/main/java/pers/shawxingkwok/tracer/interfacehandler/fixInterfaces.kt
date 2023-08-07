package pers.shawxingkwok.tracer.interfacehandler

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.shawxingkwok.ksputil.noPackageName
import pers.shawxingkwok.ksputil.simpleName
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.util.isMyOpen
import pers.shawxingkwok.tracer.shared.getRootNodesKSClasses
import pers.shawxingkwok.tracer.shared.tracerInterfaces
import java.io.File

private val cache =
    mutableMapOf<
//      interface ksClass,
        KSClassDeclaration,
//      All (ksProp name,   parent ksClass,       is open)
        Map<String, Pair<KSClassDeclaration, Boolean>>
    >()

private var called = false

// Override cognominal open ksProperties from different super interfaces.
internal fun fixInterfaces(){
    require(!called)
    called = true

    getRootNodesKSClasses()
        .flatMap { it.tracerInterfaces.toList() }
        .forEach(::fixInterface)

    cache.clear()
    Tags.interfacesFixed = true
}

// `ksClass` is the tracer interface
private fun fixInterface(ksClass: KSClassDeclaration) {
    if (ksClass in cache) return

    val superInterfaceKSClasses = ksClass.superTypes.map { it.resolve().declaration as KSClassDeclaration }.toList()
    superInterfaceKSClasses.forEach(::fixInterface)

    val map = mutableMapOf<String, Pair<KSClassDeclaration, Boolean>>()
    cache[ksClass] = map
    superInterfaceKSClasses.forEach { map += cache[it]!! }

    map += ksClass.getDeclaredProperties().associate {
        it.simpleName() to (ksClass to it.isMyOpen())
    }

    if (superInterfaceKSClasses.count() != 2) return

    val (first, second) = superInterfaceKSClasses
    val (firstCache, secondCache) = superInterfaceKSClasses.map { cache[it]!! }

    val selfKSPropNames = ksClass.getDeclaredProperties().map { it.simpleName() }.toList()
    val insertedLines = firstCache.keys
        .intersect(secondCache.keys)
        .asSequence()
        .filterNot { it in selfKSPropNames }
        .filterNot { ksPropName ->
            val firstKSClass = firstCache[ksPropName]!!.first
            val secondKSClass = secondCache[ksPropName]!!.first
            if (firstKSClass == secondKSClass) return@filterNot true
            val firstType = firstKSClass.asStarProjectedType()
            val secondType = secondKSClass.asStarProjectedType()
            firstType.isAssignableFrom(secondType) || secondType.isAssignableFrom(firstType)
        }
        .mapNotNull { ksPropName ->
            when{
                // is open
                firstCache[ksPropName]!!.second  -> ksPropName to first
                secondCache[ksPropName]!!.second -> ksPropName to second
                else -> null
            }
        }
        // Add these new property info to cache.
        .onEach { (ksPropName, _) -> map[ksPropName] = ksClass to true }
        .map { (ksPropName, superKSClass) ->
            "    override val `$ksPropName` get() = super<${superKSClass.noPackageName()}>.`$ksPropName`"
        }
        .toList()

    if (insertedLines.none()) return

    val file = ksClass.containingFile!!.filePath.let(::File)
    val lines = file.readLines().toMutableList()
    val i =
        if (ksClass == ksClass.containingFile!!.declarations.first())
            lines.indexOf("}")
        else
            lines.lastIndexOf("}")

    lines.addAll(i, insertedLines)
    file.writeText(lines.joinToString("\n"))
}