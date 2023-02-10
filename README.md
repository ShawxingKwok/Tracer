# Tracer
Generates extensional properties orienting to inner traceable elements, and replaces traditional 
dependency injection tools like dagger, kodein and koin.

## Preliminary Notes
`Kotlin Native` and `Kotlin JS` are not supported. Because they lack `context receiver` which is 
essential in this tool.

There is an android extension at [tracer-android-traditional](https://github.com/ApolloKwok/TracerAndroidTraditional)
for you to refer after you learned this.

## Usage


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
Skip to [configure tracer](#configure-tracer) if your ksp plugin version is '1.8.0-1.0.9' or 
higher.  
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

## Deficiency and its expected resolution by IDE

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