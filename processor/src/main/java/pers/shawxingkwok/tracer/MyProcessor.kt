package pers.shawxingkwok.tracer

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import jdk.internal.org.objectweb.asm.TypeReference
import pers.shawxingkwok.ksputil.*
import pers.shawxingkwok.tracer.interfacehandler.buildConverters
import pers.shawxingkwok.tracer.interfacehandler.buildInterface
import pers.shawxingkwok.tracer.interfacehandler.fixInterfaces
import pers.shawxingkwok.tracer.prophandler.PropsBuilder
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.shared.getRootNodesKlasses
import pers.shawxingkwok.tracer.util.checkUsages
import pers.shawxingkwok.tracer.util.getPreNeededProperties
import pers.shawxingkwok.tracer.util.insideModuleVisibleKlasses
import pers.shawxingkwok.tracer.util.myValidate
import java.util.Collections.emptyList

internal object MyProcessor : KSProcessor {
    class Provider : KSProcessorProvider({ MyProcessor })
    
    init { checkUsages() }

    private lateinit var invalidRootNodesTypeParameterInfo: List<Pair<String, KSTypeParameter>>
    private lateinit var invalidSymbolsInfo: List<Pair<KSClassDeclaration, List<Int>>>

    private fun KSClassDeclaration.getBeingCheckedSymbols() =
        typeParameters + superTypes + getDeclaredProperties()

    override fun process(times: Int): List<KSAnnotated> = when {
        !Tags.interfacesBuilt -> {
            invalidRootNodesTypeParameterInfo =
                if (!MyProcessor::invalidRootNodesTypeParameterInfo.isInitialized)
                    getRootNodesKlasses().flatMap { klass ->
                        klass.typeParameters
                            .filterNot { it.myValidate() }
                            .map { klass.qualifiedName()!! to it }
                    }
                else
                    invalidRootNodesTypeParameterInfo.filterNot{ (klassName, param) ->
                        resolver.getClassDeclarationByName(klassName)!!
                            .typeParameters
                            .first { it.simpleName() == "$param" }
                            .myValidate()
                    }

            if (invalidRootNodesTypeParameterInfo.none()) {
                getRootNodesKlasses().forEach(::buildInterface)
                Tags.interfacesBuilt = true
            }
            getRootNodesKlasses()
        }

        !Tags.propsBuilt -> {
            if (!Tags.interfacesFixed) {
                buildConverters()
                fixInterfaces()

                // warn if some classes with @Root/Nodes don't implement their tracer interfaces.
                val notImplementedKlasses = getRootNodesKlasses().filter { klass ->
                    klass.superTypes.all {
                        !it.resolve().declaration.isAnnotationPresent(TracerGeneration.Interface::class)
                    }
                }
                if (notImplementedKlasses.any())
                    Log.w(
                        msg = "Let classes below implement corresponding tracer interfaces.",
                        symbols = notImplementedKlasses.toTypedArray()
                    )
            }

            // update invalid symbols
            invalidSymbolsInfo = when{
                 !MyProcessor::invalidSymbolsInfo.isInitialized ->
                     (resolver.getAllFiles().toSet() - resolver.getNewFiles().toSet())
                    .flatMap { it.declarations }
                    .insideModuleVisibleKlasses()
                    .map { klass ->
                        klass to klass.getBeingCheckedSymbols().mapIndexedNotNull{ i, symbol->
                            val needed = when(symbol){
                                is KSTypeParameter -> true

                                is KSTypeReference -> !symbol.isAnnotationPresent(Tracer.Omit::class)

                                is KSPropertyDeclaration -> symbol in klass.getPreNeededProperties()

                                else -> error("")
                            }

                            i.takeIf { needed && !symbol.myValidate() }
                        }
                    }

                else ->
                    invalidSymbolsInfo.map { (oldKlass, oldIndices)->
                        val newKlass = resolver.getClassDeclarationByName(oldKlass.qualifiedName!!)!!
                        val symbols = newKlass.getBeingCheckedSymbols()
                        newKlass to oldIndices.filterNot { symbols[it].myValidate() }
                    }
            }
            .filter { (_, indices)-> indices.any() }

            // continue processing next round if some invalid symbols remain
            if (invalidSymbolsInfo.any())
                getRootNodesKlasses()
            // otherwise build new props
            else {
                getRootNodesKlasses().forEach(::PropsBuilder)
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
                msg = "Type parameters below, essential for building tracer interfaces, are invalid.",
                symbols = invalidRootNodesTypeParameterInfo.map { it.second }.toTypedArray(),
            )

        if (Tags.interfacesBuilt
            && MyProcessor::invalidSymbolsInfo.isInitialized
            && invalidSymbolsInfo.any()
        )
            Log.e("Tracer properties building are stopped because some invalid symbols.")
    }
}