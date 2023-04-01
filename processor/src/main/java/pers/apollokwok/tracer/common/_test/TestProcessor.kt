package pers.apollokwok.tracer.common._test

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import com.google.devtools.ksp.visitor.KSValidateVisitor
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.typesystem.*
import pers.apollokwok.tracer.common.typesystem.getSrcKlassTraceableSuperTypes
import java.awt.LinearGradientPaint
import javax.swing.text.html.HTML.Tag.I
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
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

public class TestProcessor : KspProcessor{
    override fun process(times: Int): List<KSAnnotated> {
//        val prop = resolver.getPropertyDeclarationByName("MultiBounds.myList")!!
//        val s = prop.getTraceableTypes().last() as Type.Specific
//        Log.w(s.getContent { true })
        return emptyList()
    }

    public class Provider : KspProvider(::TestProcessor)
}

private fun KClass<*>.convert(): KSClassDeclaration = resolver.getClassDeclarationByName(qualifiedName!!)!!

private fun KFunction<*>.convert(): KSFunctionDeclaration{
    val path =
        when(val clazz = javaMethod!!.declaringClass){
            null -> name
            else -> clazz.canonicalName + "." + name
        }
    return resolver.getFunctionDeclarationsByName(path).first()
}

private fun KProperty<*>.convert(): KSPropertyDeclaration {
    val clazz = this.javaGetter!!.declaringClass

    return resolver.getPropertyDeclarationByName(clazz.canonicalName + "." + name)
            ?: error("Top-level properties can't be got.")
}