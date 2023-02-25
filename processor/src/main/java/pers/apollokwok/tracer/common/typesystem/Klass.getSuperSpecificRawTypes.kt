package pers.apollokwok.tracer.common.typesystem

import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import pers.apollokwok.ksputil.alsoRegister
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.ktutil.updateIf
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.annotations.TracerInterface
import pers.apollokwok.tracer.common.util.isAnnotatedRootOrNodes
import pers.apollokwok.tracer.common.util.isNative
import pers.apollokwok.tracer.common.util.moduleVisibility

private val cache = mutableMapOf<Pair<KSClassDeclaration, Boolean>, List<Type.Specific>>().alsoRegister()

internal fun KSClassDeclaration.getSuperSpecificRawTypes(isSrc: Boolean): List<Type.Specific> =
    if (!isNative()) emptyList()
    else cache.getOrPut(this to isSrc) {
        val currentSuperKlasses = mutableSetOf<KSClassDeclaration>()

        superTypes
            .filterNot { it.isAnnotationPresent(Tracer.Omit::class) }
            .map { typeRef ->
                // remove '?' since they may be converted from some alias types with '?'
                typeRef.toProtoWithoutAliasAndStar().updateNullability(false) as Type.Specific
            }
            .filterNot {
                it.decl.moduleVisibility() == null
                || isSrc && it.decl.isAnnotatedRootOrNodes()
                || it.decl.isAnnotationPresent(TracerInterface::class)
                || it.decl == Type.`Any？`.decl
                || it.decl in currentSuperKlasses
            }
            .onEach { currentSuperKlasses += it.decl }
            .flatMap { basicSpecificSuperType ->
                val map = basicSpecificSuperType.args.associateBy { it.param.simpleName() }

                val upperUpdatedSuperTypes = basicSpecificSuperType.decl
                    .getSuperSpecificRawTypes(isSrc)
                    .filterNot { it.decl in currentSuperKlasses }
                    .onEach { currentSuperKlasses += it.decl }
                    .map { type -> type.updateIf({ map.any() }){ it.convertGeneric(map).first } }

                listOf(basicSpecificSuperType) + upperUpdatedSuperTypes
            }
            .toList()
    }