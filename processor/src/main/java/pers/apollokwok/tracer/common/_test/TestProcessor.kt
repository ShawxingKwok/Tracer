package pers.apollokwok.tracer.common._test

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getFunctionDeclarationsByName
import com.google.devtools.ksp.getPropertyDeclarationByName
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.typesystem.getBoundProto
import pers.apollokwok.tracer.common.typesystem.getTraceableTypes
import pers.apollokwok.tracer.common.typesystem.toProto
import pers.apollokwok.tracer.common.typesystem.toProtoWithoutAliasAndStar
import javax.swing.text.html.HTML.Tag.I
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

public typealias NullableInt = Int?
public typealias XNullableInt = NullableInt

private class J<T: V, V: CharSequence?> {
    val j: J<T & Any, *> = TODO()
    val js : List<J<T & Any, *>> = TODO()
    lateinit var t: T & Any
    fun foo(){}
}

public class TestProcessor : KspProcessor{
    override fun process(times: Int): List<KSAnnotated> {
        resolver.getPropertyDeclarationByName("ArgsTest.t", true)!!.type.toProto().let { Log.w(it) }
        resolver.getPropertyDeclarationByName("ArgsTest._t", true)!!.type.toProto().let { Log.w(it) }
        resolver.getPropertyDeclarationByName("ArgsTest.pair", true)!!.type.toProto().let { Log.w(it) }
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

private fun KProperty<*>.convert(): KSPropertyDeclaration{
    val path =
        when(val clazz = this.javaField!!.declaringClass){
            null -> name
            else -> clazz.canonicalName + "." + name
        }
    return resolver.getPropertyDeclarationByName(path)!!
}