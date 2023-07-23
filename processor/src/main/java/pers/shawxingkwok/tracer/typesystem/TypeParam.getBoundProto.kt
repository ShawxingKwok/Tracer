package pers.shawxingkwok.tracer.typesystem

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import pers.shawxingkwok.ksputil.simpleName

internal fun KSTypeParameter.getBoundProto(): Type<*> =
    when(bounds.count()){
        0 -> error("Ksp version is forbidden to be lower than 1.8.0-1.0.9.")

        1 -> {
            // check recycle
            val boundType = bounds.first().resolve()
            val boundInnerParam = boundType.arguments.firstOrNull()?.type?.resolve()?.declaration as? KSTypeParameter
            if (boundInnerParam?.simpleName() == this.simpleName())
                Type.Specific(
                    decl = boundType.declaration as KSClassDeclaration,
                    args = listOf(Arg.Star(this)),
                    genericNames = emptyList(),
                    nullable = boundType.isMarkedNullable,
                    hasAlias = false,
                    hasConvertibleStar = false,
                )
            else
                bounds.first().toProto()
        }

        else -> {
            val originalTypes = bounds.map { it.toProto() }.toList()

            Type.Compound(
                types = originalTypes.map { it.updateNullability(false) },
                nullable = originalTypes.all { it.nullable }
            )
        }
    }