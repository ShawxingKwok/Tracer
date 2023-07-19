plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.ksp) apply false
    alias(kts.plugins.ksp)
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}