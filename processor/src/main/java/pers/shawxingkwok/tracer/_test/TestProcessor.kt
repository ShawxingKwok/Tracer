package pers.shawxingkwok.tracer._test

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import pers.shawxingkwok.ksputil.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

public typealias NullableInt = Int?
public typealias XNullableInt = NullableInt

private class J<T, V: CharSequence?> {
    val j: J<T & Any, *> = TODO()
    val js : List<J<T & Any, *>> = TODO()
    lateinit var t: T & Any
    fun foo(){}
    val x: Int.() -> Unit = TODO()
    val list: MutableList<Int> = TODO()
}

internal class TestProcessor : KSProcessor{
    class Provider : KSProcessorProvider(::TestProcessor)

    override fun process(round: Int): List<KSAnnotated> {
        Log.d(round)

        if (round == 0){
            val ksClass = resolver.getClassDeclarationByName("generic.X")!!
            val param = ksClass.typeParameters.last()
            val _param = ksClass
                .getDeclaredProperties()
                .first { it.simpleName() == "v" }
                .type.resolve()
                .declaration as KSTypeParameter

            Log.w(param == _param)
            Log.d(param == _param)
            Log.d(param.bounds.first().resolve())
            Log.d(_param.bounds.first().resolve())
        }
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