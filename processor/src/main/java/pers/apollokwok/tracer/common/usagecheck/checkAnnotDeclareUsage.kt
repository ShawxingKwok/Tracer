package pers.apollokwok.tracer.common.usagecheck

import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.Log
import pers.apollokwok.ksputil.getAnnotatedSymbols
import pers.apollokwok.ksputil.getAnnotationByType
import pers.apollokwok.ksputil.resolver
import pers.apollokwok.ktutil.allDo
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.util.isOverridingTracerInterface
import pers.apollokwok.tracer.common.util.myValidate
import pers.apollokwok.tracer.common.shared.Names
import pers.apollokwok.tracer.common.util.isMyAbstract
import pers.apollokwok.tracer.common.util.moduleVisibility

//those invalid @Declare(true) are needed and would be handled by 'onFinish' at last.
//those invalid @Declare(false) would be omitted
internal fun checkAnnotDeclareUsage(): List<KSAnnotated> {
    // todo: require each @Declare on type must be with `false` after annotations on types can be got.
    val (validProps, invalidProps) = resolver.getAnnotatedSymbols<Tracer.Declare, KSPropertyDeclaration>()
        .partition { it.myValidate() == true }

    val wronglyAnnotatedPropInfo =
        validProps.map { prop ->
            val reasons = mutableListOf<String>()

            allDo(
                (prop.parentDeclaration == null) to "top-level",

                ((prop.parentDeclaration as? KSClassDeclaration)?.classKind == ClassKind.INTERFACE) to
                    "in interfaces",

                (prop.moduleVisibility() == null) to "module-invisible",

                (prop.extensionReceiver != null) to "has extensional receiver",

                prop.isOverridingTracerInterface() to "overrides tracer interface property",
            ) { (isError, reason) ->
                if (isError) reasons += reason
            }

            prop to reasons
        }
        .filter {(_, reasons)-> reasons.any() }

    Log.require(wronglyAnnotatedPropInfo.none(), emptyList()){
        wronglyAnnotatedPropInfo.joinToString(
            prefix = "Below properties are always omitted in tracer building, " +
                "which means annotating ${Names.Declare} on them makes no sense.\n",
            separator = "\n",
        ){ (prop, reasons) ->
            val location = prop.location as FileLocation
            "$reasons $prop at ${location.filePath}:${location.lineNumber}."
        }
    }

    // require @Declare(true) on specific situations.
    val wronglyDeclaredTrueProps = validProps
        .filter { it.getAnnotationByType<Tracer.Declare>()!!.enabled }
        .filter { it.isMyAbstract() || it.isDelegated() || it.hasBackingField }

    Log.require(wronglyDeclaredTrueProps.none(), wronglyDeclaredTrueProps){
        "@${Names.Declare} or @${Names.Declare}(true) should be used on " +
        "open or final properties which are not delegated and without fields."
    }

    return invalidProps
}