package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.ktutil.Bug

private val cache = mutableMapOf<String, Modifier>()

private val KSDeclaration.status: Modifier get() = cache.getOrPut(qualifiedName()!!) {
    when {
        // consider that bug first: modifiers in the java @interface declaration contain ABSTRACT
        this is KSClassDeclaration && classKind == ClassKind.ANNOTATION_CLASS -> Modifier.FINAL

        // return directly if its modifiers contain any key modifier
        Modifier.FINAL in modifiers || Modifier.JAVA_STATIC in modifiers -> Modifier.FINAL
        Modifier.OPEN in modifiers || Modifier.JAVA_DEFAULT in modifiers -> Modifier.OPEN
        Modifier.SEALED in modifiers || Modifier.ABSTRACT in modifiers -> Modifier.ABSTRACT

        // situations below are property or function with no FINAL, OPEN or ABSTRACT
        this is KSClassDeclaration ->
            when (classKind) {
                ClassKind.INTERFACE -> Modifier.ABSTRACT

                ClassKind.CLASS ->
                    // default kotlin class
                    if (origin == Origin.KOTLIN || origin == Origin.KOTLIN_LIB)
                        Modifier.FINAL
                    else // default java class
                        Modifier.OPEN

                // object, annotation, enum
                else -> Modifier.FINAL
            }

        // this is property or function
        else -> {
            val parentKlass = parentDeclaration as? KSClassDeclaration

            when (parentKlass?.classKind) {
                // on top level
                null -> Modifier.FINAL

                ClassKind.INTERFACE -> when (this) {
                    // constant in java interface was considered above
                    // and here is from only kotlin
                    is KSPropertyDeclaration ->
                        if (Modifier.ABSTRACT in getter!!.modifiers)
                            Modifier.ABSTRACT
                        else
                            Modifier.OPEN

                    is KSFunctionDeclaration ->
                        // 'isAbstract' is realized in KSFunctionDeclarationImpl, but that part calls
                        // an internal function ktFunction.hasBody and can't be taken here.
                        if (isAbstract) Modifier.ABSTRACT
                        else Modifier.OPEN

                    else -> Bug()
                }

                ClassKind.CLASS -> when {
                    parentKlass.status == Modifier.FINAL -> Modifier.FINAL

                    // situations below are in open or abstract class
                    // default kotlin property or function
                    origin == Origin.KOTLIN_LIB || origin == Origin.KOTLIN ->
                        if (Modifier.OVERRIDE in modifiers)
                            Modifier.OPEN
                        else
                            Modifier.FINAL

                    // java property
                    this is KSPropertyDeclaration -> Modifier.FINAL

                    // default java function
                    this is KSFunctionDeclaration -> Modifier.OPEN

                    else -> Bug()
                }

                // in an object, annotation, or enum class
                else -> Modifier.FINAL
            }
        }
    }
}

private fun KSDeclaration._isMyOpen() = status == Modifier.OPEN
private fun KSDeclaration._isMyAbstract() = status == Modifier.ABSTRACT
private fun KSDeclaration._isFinal() = status == Modifier.FINAL

/**
 *
 */
internal fun KSClassDeclaration.isMyOpen(): Boolean = _isMyOpen()
internal fun KSClassDeclaration.isMyAbstract(): Boolean = _isMyAbstract()
internal fun KSClassDeclaration.isFinal(): Boolean = _isFinal()

internal fun KSPropertyDeclaration.isMyOpen(): Boolean = _isMyOpen()
internal fun KSPropertyDeclaration.isMyAbstract(): Boolean = _isMyAbstract()
@Suppress("unused")
internal fun KSPropertyDeclaration.isFinal(): Boolean = _isFinal()