package pers.apollokwok.tracer.common._test

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.getPropertyDeclarationByName
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import pers.apollokwok.ksputil.*
import pers.apollokwok.tracer.common.typesystem.*
import pers.apollokwok.tracer.common.typesystem.Type
import pers.apollokwok.tracer.common.typesystem.getBoundProto
import pers.apollokwok.tracer.common.typesystem.getTraceableTypes
import pers.apollokwok.tracer.common.typesystem.toProto
import pers.apollokwok.tracer.common.typesystem.toProtoWithoutAliasAndStar
import pers.apollokwok.tracer.common.util.isCommon

internal class TestProcessor : KspProcessor {
    companion object{
        private fun Any?.letLog() = Log.w(this)
    }

    override fun process(times: Int): List<KSAnnotated> {
         if (times == 1) onFirstRound()
         else onLaterRounds(times)
         return emptyList()
    }

    fun onFirstRound(): List<KSAnnotated>{
        resolver.getClassDeclarationByName("InOutPair").letLog()
        return emptyList()
    }

    fun onLaterRounds(times: Int): List<KSAnnotated>{
     return emptyList()
    }

    fun createFile(){
     Environment.codeGenerator.createFile(
         null,
         "Fd",
         Dependencies.ALL_FILES,
         "class GeneratedTest"
     )
    }

    class DefNotNull<T> : KspProcessor.Test(){
         val ls: List<T & Any> get() = TODO()
         init {
             DefNotNull::class.toKs().typeParameters.first().bounds.first().resolve().letLog()
             val decl = ::ls.toKs().type.resolve().arguments.first().type!!.resolve().declaration
             Log.w(decl is KSTypeParameter)
             Log.w(decl == DefNotNull::class.toKs().typeParameters.first())
         }
    }
    class Param<A: CharSequence, B: A> : KspProcessor.Test(){
     lateinit var a: A
     lateinit var param: Param<in String, *>

     init {
         this::class.toKs().typeParameters.first().qualifiedName().letLog()
         ::a.toKs().type.resolve().declaration.qualifiedName().letLog()
     }
    }

    class IsCommon() : KspProcessor.Test(){
     lateinit var map: Map<String, CharArray>
     init {
         ::map.toKs().getTraceableTypes().first().isCommon().letLog()
     }
    }

    class Star<T> : KspProcessor.Test() where T: CharSequence, T: Any {
     lateinit var t: T

     init {
         val type = ::t.toKs().getTraceableTypes().first()
         Log.w(type is Type.Compound)
         if (type is Type.Compound){
             Log.w(type.isDeclarable)
         }
     }
    }

    fun testNullability(){
     val type = resolver.getPropertyDeclarationByName("GenericContainer.list")!!.getTraceableTypes().first() as Type.Specific
    //        type.getName {
    //            it.contractedName + "_" + it.packageName().replace(".", "")
    //        }.letLog()
    //        type.getGrossKey { it.qualifiedName()!! }.letLog()
    //        type.getContent().letLog()
    }

    fun testConvertAlias(){
     val myDoublePair = resolver.getPropertyDeclarationByName("myDoublePair")!!.type.toProto()
     Log.w(myDoublePair)
     Log.w(myDoublePair.convertAlias())
    }

    fun testConvertStar(){
     val proto = resolver.getPropertyDeclarationByName("fag")!!.type.toProto()
     Log.w(proto)
     Log.w(proto.convertStar())
    }

    class Recycle : KspProcessor.Test(){
     internal interface J<T: Enum<T>>{
         var t: T
     }
     lateinit var j: J<*>
     lateinit var _j: J<out Enum<*>>

     init {
         ::j.toKs().type.toProto().convertStar().letLog()
         ::_j.toKs().type.toProto().convertStar().letLog()
     }
    }

    fun testComprehensively(){
        val prop = resolver.getPropertyDeclarationByName("ComprehensiveContainer.c!!omprehensive")!!
        prop.type.toProtoWithoutAliasAndStar().letLog()
        Log.w(prop.getTraceableTypes())
    }

    fun testPassingVariancesFromInwardToOutWard(){
     resolver.getPropertyDeclarationByName("myHuman")!!.getTraceableTypes().drop(1).letLog()
    }

    fun testAliasNullability(){
     resolver.getPropertyDeclarationByName("strs")!!.type.resolve().arguments.first().type!!
         .resolve().isMarkedNullable.letLog()

     resolver.getPropertyDeclarationByName("str")!!.type.resolve().isMarkedNullable.letLog()
     resolver.getPropertyDeclarationByName("_str")!!.type.resolve().isMarkedNullable.letLog()
    }

    fun testGetSuperSpecificTypesContainingT(){
     val klass = resolver.getClassDeclarationByName("SuperTypesPassingTest")!!
     val map = klass.typeParameters.associate { param ->
         val type = (param.getBoundProto().convertGeneric(
             emptyMap()).first.convertAlias().convertStar() as Type.Specific
                 )
    //                .copy(genericName = param.simpleName())

         param.simpleName() to Arg.Out(type, param)
     }

    //        klass.getSuperSpecificTypesContainingT().map { it.replaceGeneric(map) }
    //            .letLog()
    }

    fun testConvertStarInSuperType(){
     resolver.getClassDeclarationByName("OSG")!!.superTypes.first().toProto().convertStar()
         .letLog()
    }

    // TODO: 引入 alias 和 star
    @Suppress("UNCHECKED_CAST")
    fun testPassTFromSuperToSuper(){
     val klass = resolver.getClassDeclarationByName("ImImpl")!!

     val superProto = klass.superTypes.first().toProto().convertAlias()
         .convertStar() as Type.Specific
     val `2ndSuperProto` = superProto.decl.superTypes.first().toProto().convertAlias().convertStar() as Type.Specific
     val `3rdSuperProto` = `2ndSuperProto`.decl.superTypes.first().toProto().convertAlias().convertStar() as Type.Specific

     val genericMap = superProto.args.associateBy { it.param.simpleName() }
     val replaced2nd = `2ndSuperProto`.convertGeneric(genericMap as Map<String, Arg.General<*>>).first

     val `2ndGenericMap` = replaced2nd.args.associateBy { it.param.simpleName() }
     val replaced3rd = `3rdSuperProto`.convertGeneric(`2ndGenericMap` as Map<String, Arg.General<*>>)

     Log.w(replaced2nd)
     Log.w(replaced3rd)
    }

    fun testPassArgsToSuper(){
     val basicTypeRef = resolver.getPropertyDeclarationByName("gnio")!!.type
     val convertedBasicType = basicTypeRef.toProto().convertStar() as Type.Specific
     val map = convertedBasicType.args.filterIsInstance<Arg.General<*>>().associateBy { it.param.simpleName() }
     val newArgs = convertedBasicType.args.map {
         if (it is Arg.Star)
             Arg.Out(it.param.getBoundProto().convertGeneric(map).first, it.param)
         else
             it
     }
     val newMap = newArgs.associateBy { it.param.simpleName() }

     @Suppress("UNCHECKED_CAST")
     (basicTypeRef.resolve().declaration as KSClassDeclaration).superTypes.first().toProto()
         .convertGeneric(newMap as Map<String, Arg.General<*>>)
         .letLog()
    }

    fun testToProto(){
     val ii = resolver.getClassDeclarationByName("GenericI")!!.getDeclaredProperties().first().type.toProto()
     Log.w(ii)
     resolver.getPropertyDeclarationByName("genericI")!!.type.toProto().letLog()
    }

    fun testUpdateBasicT(){
     val kk = resolver.getClassDeclarationByName("KKContainer")!!.getDeclaredProperties().first().type.toProto()
     Log.w(kk)
    }

    private fun testPropTypes(){
     resolver.getClassDeclarationByName("MultiBound")!!.getDeclaredProperties().first().type.toProto()
         .letLog()
    }

    private fun testAliasTypeEquality(){
     val a = resolver.getAllFiles().first { it.fileName == "Type.kt" }.declarations.toList()[0] as KSTypeAlias
     val b = resolver.getAllFiles().first { it.fileName == "Type.kt" }.declarations.toList()[1] as KSTypeAlias

     val typeA = a.type.resolve()
     val typeB = b.type.resolve().arguments.first().type!!.resolve()
     Log.w(typeA == typeB)
    }

    private fun testAliasTypes(){
     val proto = resolver.getPropertyDeclarationByName("myDoublePair")!!.type.toProto()
     Log.w(proto)
     Log.w(proto.convertAlias())
    }

    private fun testTypeAlias(){
     resolver.getPropertyDeclarationByName("test.typealias.foo", true)!!
         .type.resolve().arguments[0].type!!.resolve()
         .declaration.letLog()

     resolver.getPropertyDeclarationByName("test.typealias.myFoo", true)!!
         .type.resolve().arguments[0].type!!.resolve()
         .declaration.letLog()

     resolver.getPropertyDeclarationByName("test.typealias.myFoo", true)!!.containingFile!!
         .declarations.toList()
         .let {
             val alias = it[it.lastIndex - 1] as KSTypeAlias
             val param = alias.type.resolve().arguments.first().type!!.resolve().declaration as KSTypeParameter
             param.bounds.toList().map { it.resolve().declaration }.letLog()
         }

     resolver.getClassDeclarationByName("kotlin.collections.List")!!.typeParameters.first()
         .bounds.toList().letLog()
    }

    private fun testSuperTypes(){
     val klass = resolver.getClassDeclarationByName("TypeTest")!!
     val prop = resolver.getPropertyDeclarationByName("typeTest")!!
     val innerProp = klass.getDeclaredProperties().first()
    }

    private fun testVariance(){
     val klass = resolver.getClassDeclarationByName("I")!!


    //        val propType = klass.getDeclaredProperties().first().type.toActual()
    //        propType.letLog()
    }
}