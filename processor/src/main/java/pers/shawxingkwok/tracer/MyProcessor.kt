package pers.shawxingkwok.tracer

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.tracer.interfacehandler.buildConverters
import pers.shawxingkwok.tracer.interfacehandler.buildInterface
import pers.shawxingkwok.tracer.interfacehandler.fixInterfaces
import pers.shawxingkwok.tracer.prophandler.PropsBuilder
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.shared.getRootNodesKSClasses
import pers.shawxingkwok.tracer.util.checkUsages
import pers.shawxingkwok.tracer.util.getPreNeededKSProperties
import pers.shawxingkwok.tracer.util.insideModuleVisibleKSClasses
import pers.shawxingkwok.tracer.util.myValidate
import java.util.Collections.emptyList

internal object MyProcessor : KSProcessor {
    class Provider : KSProcessorProvider({ MyProcessor })
    
    init { checkUsages() }

    private lateinit var invalidRootNodesTypeParameterInfo: List<Pair<String, KSTypeParameter>>
    private lateinit var invalidSymbolsInfo: List<Pair<KSClassDeclaration, List<Int>>>

    private fun KSClassDeclaration.getBeingCheckedSymbols() =
        typeParameters + superTypes + getDeclaredProperties()

    override fun process(round: Int): List<KSAnnotated> = when {
        !Tags.interfacesBuilt -> {
            invalidRootNodesTypeParameterInfo =
                if (!MyProcessor::invalidRootNodesTypeParameterInfo.isInitialized)
                    getRootNodesKSClasses().flatMap { ksClass ->
                        ksClass.typeParameters
                            .filterNot { it.myValidate() }
                            .map { ksClass.qualifiedName()!! to it }
                    }
                else
                    invalidRootNodesTypeParameterInfo.filterNot{ (ksClassName, param) ->
                        resolver.getClassDeclarationByName(ksClassName)!!
                            .typeParameters
                            .first { it.simpleName() == "$param" }
                            .myValidate()
                    }

            if (invalidRootNodesTypeParameterInfo.none()) {
                getRootNodesKSClasses().forEach(::buildInterface)
                Tags.interfacesBuilt = true
            }
            getRootNodesKSClasses()
        }

        !Tags.propsBuilt -> {
            if (!Tags.interfacesFixed) {
                buildConverters()
                fixInterfaces()

                // warn if some classes with @Root/Nodes don't implement their tracer interfaces.
                val notImplementedKSClasses = getRootNodesKSClasses().filter { kclass ->
                    kclass.superTypes.all {
                        !it.resolve().declaration.isAnnotationPresent(TracerGeneration.Interface::class)
                    }
                }
                if (notImplementedKSClasses.any())
                    Log.w(
                        obj = "Let classes below implement corresponding tracer interfaces.",
                        symbols = notImplementedKSClasses.toTypedArray()
                    )
            }

            // update invalid symbols
            invalidSymbolsInfo = when{
                 !MyProcessor::invalidSymbolsInfo.isInitialized ->
                     (resolver.getAllFiles().toSet() - resolver.getNewFiles().toSet())
                    .flatMap { it.declarations }
                    .insideModuleVisibleKSClasses()
                    .map { ksClass ->
                        ksClass to ksClass.getBeingCheckedSymbols().mapIndexedNotNull{ i, symbol->
                            val needed = when(symbol){
                                is KSTypeParameter -> true

                                is KSTypeReference -> !symbol.isAnnotationPresent(Tracer.Omit::class)

                                is KSPropertyDeclaration -> symbol in ksClass.getPreNeededKSProperties()

                                else -> error("")
                            }

                            i.takeIf { needed && !symbol.myValidate() }
                        }
                    }

                else ->
                    invalidSymbolsInfo.map { (oldKSClass, oldIndices) ->
                        val newKSClass = resolver.getClassDeclarationByName(oldKSClass.qualifiedName!!)!!
                        val symbols = newKSClass.getBeingCheckedSymbols()
                        newKSClass to oldIndices.filterNot { symbols[it].myValidate() }
                    }
            }
            .filter { (_, indices) -> indices.any() }

            // continue processing next round if some invalid symbols remain
            if (invalidSymbolsInfo.any())
                getRootNodesKSClasses()
            // otherwise build new props
            else {
                getRootNodesKSClasses().forEach(::PropsBuilder)
                Tags.propsBuilt = true
                emptyList()
            }
        }

        else -> emptyList()
    }

    // report if there remain some invalid symbols.
    override fun onFinish() {
        if (invalidRootNodesTypeParameterInfo.any())
            Log.e(
                obj = "Type parameters below, essential for building tracer interfaces, are invalid.",
                symbols = invalidRootNodesTypeParameterInfo.map { it.second }.toTypedArray(),
            )

        if (Tags.interfacesBuilt
            && MyProcessor::invalidSymbolsInfo.isInitialized
            && invalidSymbolsInfo.any()
        )
            Log.e("Tracer properties building are stopped because some invalid symbols.")
    }
}