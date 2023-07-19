package pers.shawxingkwok.tracer

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.ktutil.Unreachable
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.shawxingkwok.tracer.interfacehandler.buildConverters
import pers.shawxingkwok.tracer.interfacehandler.buildInterfaces
import pers.shawxingkwok.tracer.interfacehandler.fixInterfaces
import pers.shawxingkwok.tracer.prophandler.PropsBuilder
import pers.shawxingkwok.tracer.shared.Names
import pers.shawxingkwok.tracer.shared.Tags
import pers.shawxingkwok.tracer.shared.getRootNodesKlasses
import pers.shawxingkwok.tracer.util.checkUsages
import pers.shawxingkwok.tracer.util.getPreNeededProperties
import pers.shawxingkwok.tracer.util.insideModuleVisibleKlasses
import pers.shawxingkwok.tracer.util.myValidate
import java.util.Collections.emptyList

internal object MyProcessor : KspProcessor {
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
                            .filterNot { it.myValidate() == true }
                            .map { klass.qualifiedName()!! to it }
                    }
                else
                    invalidRootNodesTypeParameterInfo.filterNot{ (klassName, param) ->
                        resolver.getClassDeclarationByName(klassName)!!
                            .typeParameters
                            .first { it.simpleName() == "$param" }
                            .myValidate() == true
                    }

            if (invalidRootNodesTypeParameterInfo.none()) buildInterfaces()
            getRootNodesKlasses()
        }

        !Tags.propsBuilt -> {
            if (!Tags.interfacesFixed) {
                buildConverters()
                fixInterfaces()

                // warn if some classes with @Root/Nodes don't implement their tracer interfaces.
                val notImplementedKlasses = getRootNodesKlasses().filter { klass ->
                    klass.superTypes.all {
                        !it.resolve().declaration.isAnnotationPresent(TracerInterface::class)
                    }
                }
                if (notImplementedKlasses.any())
                    Log.w(
                        msg = "Let classes below implement corresponding tracer interfaces.",
                        symbols = notImplementedKlasses
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

                                else -> Unreachable()
                            }

                            i.takeIf { needed && symbol.myValidate() != true }
                        }
                    }

                else ->
                    invalidSymbolsInfo.map { (oldKlass, oldIndices)->
                        val newKlass = resolver.getClassDeclarationByName(oldKlass.qualifiedName!!)!!
                        val symbols = newKlass.getBeingCheckedSymbols()
                        newKlass to oldIndices.filterNot { symbols[it].myValidate() == true }
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

    // report if there remain some unsupported symbols.
    // other invalid symbols would be hinted by IDE
    override fun onFinish() {
        if (invalidRootNodesTypeParameterInfo.any())
            Log.errorLater(
                msg = "Type parameters below, essential for build tracer interfaces, are invalid.",
                symbols = invalidRootNodesTypeParameterInfo.map { it.second },
            )

        if (!MyProcessor::invalidSymbolsInfo.isInitialized || Tags.propsBuilt) return

        // no 'true', some are false, others are `null`
        val (failedReferringSymbols, unsupportedSymbols) =
            invalidSymbolsInfo.flatMap { (klass, indices)->
                klass.getBeingCheckedSymbols().filterIndexed { i, _ -> i in indices  }
            }
            .partition { it.myValidate() == false }

        if (failedReferringSymbols.any())
            Log.errorLater(
                msg = "Symbols below are invalid probably because of unknown references.",
                symbols = failedReferringSymbols
            )

        if (unsupportedSymbols.any())
            Log.errorLater(
                symbols = unsupportedSymbols,
                msg = buildString{
                    append("Symbols below contain unsupported syntaxes")

                    val unsupportedSyntaxes = mutableListOf<String>()
                    if (!Environment.compilerVersion.isAtLeast(1, 8))
                        unsupportedSyntaxes += "T & Any"

                    if (unsupportedSyntaxes.any())
                        append(" like $unsupportedSyntaxes")

                    append(". ")

                    append(
                        "Use another declared style or annotate them with @${Names.Omit} " +
                        "if it's not your fault."
                    )
                },
            )
    }

    class Provider : KspProvider({ MyProcessor })
}