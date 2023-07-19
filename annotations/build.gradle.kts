import org.gradle.internal.impldep.org.hamcrest.core.Is
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.publish)
}

kotlin{
    jvmToolchain(8)
}

tasks.withType<KotlinCompile>().configureEach{
    kotlinOptions {
        freeCompilerArgs += "-Xexplicit-api=warning"
    }
}

// publish
mavenPublishing {
    // TODO remove suffix and set apollokwok to shawxingkwok
    val isSNAPSHOT = true
    val version = "1.0.0"
    val artifactId = "tracer-annotations"

    coordinates("io.github.apollokwok", artifactId, if (isSNAPSHOT) "$version-SNAPSHOT" else version)

    pom {
        name.set("TracerAnnotations")
        description.set("Tracer annotations")
        inceptionYear.set("2023")

        val repo = "Tracer"
        url.set("https://github.com/ShawxingKwok/$repo/")

        scm{
            connection.set("scm:git:git://github.com/ShawxingKwok/$repo.git")
            developerConnection.set("scm:git:ssh://git@github.com/ShawxingKwok/$repo.git")
        }
    }
}