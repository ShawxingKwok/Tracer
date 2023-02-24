package pers.apollokwok.tracer.common

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.ktutil.Unreachable
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.apollokwok.tracer.common.interfacehandler.buildInterface
import pers.apollokwok.tracer.common.interfacehandler.fixInterfaces
import pers.apollokwok.tracer.common.prophandler.PropsBuilder
import pers.apollokwok.tracer.common.shared.Names
import pers.apollokwok.tracer.common.shared.Tags
import pers.apollokwok.tracer.common.shared.getRootNodesKlasses
import pers.apollokwok.tracer.common.util.checkUsages
import pers.apollokwok.tracer.common.util.getPreNeededProperties
import pers.apollokwok.tracer.common.util.insideModuleVisibleKlasses
import pers.apollokwok.tracer.common.util.myValidate
import java.util.Collections.emptyList

internal object MyProcessor : KspProcessor {
    private lateinit var invalidSymbolsInfo: List<Pair<KSClassDeclaration, List<Int>>>

    private fun KSClassDeclaration.getBeingCheckedSymbols() =
        typeParameters + superTypes + getDeclaredProperties()

    var times = 0
        private set

    override fun process(times: Int): List<KSAnnotated> {
        this.times = times

        return when {
            times == 1 -> {
                val valid = checkUsages()
                if (!valid)
                    // already logger.errorLater in 'checkUsages`
                    return emptyList()
                getRootNodesKlasses().forEach(::buildInterface)
                Tags.interfacesBuilt = true
                getRootNodesKlasses()
            }

            !Tags.propsBuilt -> {
                // fix interfaces for conflict of cognominal properties from different super interfaces.
                // and warn if some classes with @Root/Nodes don't implement their tracer interfaces.
                if (times == 2) {
                    fixInterfaces()

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
                invalidSymbolsInfo = when(times){
                    2 -> (resolver.getAllFiles().toSet() - resolver.getNewFiles().toSet())
                        .flatMap { it.declarations }
                        .insideModuleVisibleKlasses()
                        .map { klass ->
                            klass to klass.getBeingCheckedSymbols().mapIndexedNotNull{ i, symbol->
                                val needed = when(symbol){
                                    is KSTypeParameter -> true

                                    is KSTypeReference -> !symbol.isAnnotationPresent(Tracer.Omitted::class)

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

                // process next round if some invalid symbols remain
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
    }

    // report if there remain some invalid symbols.
    override fun onFinish() {
        if (!MyProcessor::invalidSymbolsInfo.isInitialized || Tags.propsBuilt) return

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

        if (unsupportedSymbols.any()) {
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
                        "Use another declared style or annotate them with @${Names.Omitted} " +
                        "if it's not your fault."
                    )
                },
            )
        }
    }

    class Provider : KspProvider({ MyProcessor })
}