package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.ksputil.simpleName
import pers.apollokwok.tracer.common.MyProcessor
import pers.apollokwok.tracer.common.shared.Names
import pers.apollokwok.tracer.common.shared.contractedName
import pers.apollokwok.tracer.common.shared.getInterfaceNames
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.toProtoWithoutAliasAndStar

private val cache = mutableMapOf<String, Boolean>()

// can't be used in 1st round to ensure all tracer interfaces have been built.
internal fun KSPropertyDeclaration.isOverridingTracerInterface(): Boolean =
    cache.getOrPut(qualifiedName()!!) {
        require(MyProcessor.times >= 2)
        if (Modifier.OVERRIDE !in modifiers) return false
        if (type.myValidate() != true) return false
        val typeKlass = (type.toProtoWithoutAliasAndStar() as? Type.Specific)?.decl ?: return false
        if (!typeKlass.isAnnotatedRootOrNodes()) return false
        val (interfaceName, outerInterfaceName) = getInterfaceNames(typeKlass)

        return arrayOf("__" to outerInterfaceName, "_" to interfaceName)
            .any { (prefix, tracerInterfaceName) ->
                simpleName().startsWith(prefix)
                && typeKlass.contractedName == simpleName().substringAfter(prefix)
                // starProjectedType.isAssignableFrom() can't be used here because some type parameters may be
                // invalid then.
                && (parentDeclaration!! as KSClassDeclaration).getAllSuperTypes().any {
                    it.declaration.qualifiedName() == "${Names.GENERATED_PACKAGE}.$tracerInterfaceName"
                }
            }
    }