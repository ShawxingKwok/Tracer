package pers.shawxingkwok.tracer._test

import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

public typealias NullableInt = Int?
public typealias XNullableInt = NullableInt

private class J<T: V, V: CharSequence?> {
    val j: J<T & Any, *> = TODO()
    val js : List<J<T & Any, *>> = TODO()
    lateinit var t: T & Any
    fun foo(){}
    val x: Int.()->Unit = TODO()
    val list: MutableList<Int> = TODO()
}

internal class TestProcessor : KSProcessor{
    class Provider : KSProcessorProvider(::TestProcessor)

    override fun process(times: Int): List<KSAnnotated> {
        if (times == 1)
            Environment.codeGenerator.createFile(
                packageName = null,
                fileName = "allFileNames",
                dependencies = Dependencies(true),
                content = resolver.getAllFiles().joinToString { it.fileName },
                extensionName = "",
            )

        return emptyList()
    }
}

private fun KClass<*>.convert(): KSClassDeclaration = resolver.getClassDeclarationByName(qualifiedName!!)!!

private fun KFunction<*>.convert(): KSFunctionDeclaration{
    val clazz = this.javaMethod!!.declaringClass

    return resolver.getFunctionDeclarationsByName(clazz.canonicalName + "." + name).firstOrNull()
           ?: resolver.getFunctionDeclarationsByName(clazz.`package`.name + "." + name, true).first()
}

private fun KProperty<*>.convert(): KSPropertyDeclaration {
    val clazz = this.javaGetter!!.declaringClass

    return resolver.getPropertyDeclarationByName(clazz.canonicalName + "." + name)
           ?: resolver.getPropertyDeclarationByName(clazz.`package`.name + "." + name, true)!!
}