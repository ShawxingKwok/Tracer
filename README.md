# Tracer(Not released yet)
Generates extensional properties orienting to inner traceable elements, and replaces traditional 
dependency injection tools like dagger, kodein and koin.

## Effect
in gif

## Setup
Configure your `build.gradle` as below.

### add the ksp plugin
```groovy
plugins{
    // Assuming your kotlin version is `1.7.21`, here uses the latest ksp plugin version beginning 
    // with `1.7.21` ('1.7.21-1.0.8').  
    id 'com.google.devtools.ksp' version '1.7.21-1.0.8'
}
```

### add source sets
Skip to [configure tracer](#configure-tracer) if your ksp plugin version is '1.8.0-1.0.9' or higher.  
This part is different if you are using IntelliJ IDEA and KSP in a Gradle plugin. See [kotlin 
ksp quickstart](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code)
```groovy
// Omissible if your ksp plugin version is '1.8.0-1.0.9' or higher. 
kotlin.sourceSets {
    main.kotlin.srcDirs += "$buildDir/generated/ksp/main/kotlin"
    test.kotlin.srcDirs += "$buildDir/generated/ksp/test/kotlin"
}
```

### configure tracer 
Add this part directly, rather than insert messily. 
```groovy
//region tracer
// options
//ksp{
//    arg("tracer.allInternal", "")
//    arg("tracer.propertiesFullName", "")
//}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions.freeCompilerArgs += '-Xcontext-receivers'
}

dependencies {
    // Keep this version latest but the prefix can't be higher than your kotlin version. 
    ksp 'io.github.apollokwok:tracer-common-compiler:1.7.20-1.1.0'
    // Keep this version latest but not higher than the version above. 
    implementation 'io.github.apollokwok:tracer-common-annotations:1.7.20-1.1.0'
}
//endregion 
```

### 

## Usage
in video
找生成的代码时不用 ` 开头
val xx get() = 某个 tracerPro 前的修饰符只能为 private
如果一个类如果可能在 mallTracer 或者 out mallTracer 下，那么直接 @Nodes(Mall::class)
解释 common type 的标准
alias type 中带 * 可能会因为 multi-bounds 导致结果不准确
因为每次输入_时出来的提示较多，所以建议在外部用 private val xx get()＝声明
用 get()= 不用 =，这样则不用关心其是否为 mutable
某些 property 会被忽略
对于带泛型 / open / abstract class, 因为复用率高，不会从 property type trace, 对于会被多次构造的 class，应让 programmer 尽量少去探索次内部的东西

## Notes
1, `Kotlin Native` and `Kotlin JS` are not supported. Because they lack `context receiver` which is
essential in this tool.

2, If you are developing android, see [tracer extension on traditional android](https://github.com/ApolloKwok/TracerAndroidTraditional)
after you learned this.

3, Syntax `T & Any` is not allowed until ksp version `1.8.0-1.0.9`. In old versions, you could 
annotate those traceable properties or super types with `@Tracer.Declare(false)` to omit them.

4, `Tracer` is compatible with `ksp 1.7.0-1.0.6` at least. Although mostly it works well with 
`ksp`, you'd better try to make your `ksp plugin` in a high stable version, since `ksp api` is not 
stable and many bugs are fixed every version. 

5, Java files are forbidden to use `Tracer`, because I don't want to spend time analyzing those 
outdated things. 

6, Few generated types fail code inspection, mostly because of the imperfect authoritative type 
inference system. Find their corresponding source properties or super types, then use another type
or annotate them with`@Tracer. Declare(false)`.
e.g.: todo 

7, 如果 super abstract class 和 self 均有 Root /Nodes 标记，那么 super abstract class 中最好不要 override self

## Deficiency and its expected resolution by IDE or new Kotlin plugin.
In gif.

Too many hints when you input `_X.` in a big project. 
(after the visibility, of functions with context receivers, is fixed -> new generated tracer 
property would replace the receiver with context receiver. Then there wouldn't be redundant 
hints.)  

## Expect integration with new Kotlin syntax
Implement tracer interfaces automatically.

Override and solve the conflict automatically.

## Sample optimization
My sample misses some important details but I have no good idea to add corresponding Atm functions 
in moderate size. Welcome to optimize it or donate a demo small but displaying all important tracer 
details!

## My Words
Code in this project is probably the globally best at present for its functionality, creativity,
difficulty, structure, efficiency, concision, and beauty. Wish you could learn from it！

This tool would help manage memory well assuming Kotlin is without GC, because most nodes only need
to manage its own element memories. In the near future, programming languages would be unified and
the whole IT development structure would be rebuilt.

In addition to this, I have two other halfway done works and am waiting for some company to help make
IDE plugins. I have sent resumes to common top internet companies, but those limited resume sizes are
too small and they didn't give me opportunities to display.

At last, if you are familiar with IT, feel hard to learn Kotlin and love Java or Go more, your logic
ability mustn't be well enough for you to engage in IT. If so, do something you love and must excel
at. I have no offensive intention, just meaning everyone has his strengths and weaknesses, and every
job has its threshold.