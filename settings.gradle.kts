pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // snapshot
        maven{ url "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
    }
}

rootProject.name = "Tracer"

include ':annotations'
//include ':compiler'
//include ':compiler:unittest'
//include ':compiler:overalltest'
//include ':processor'
//include ':test:overall'
//include ':test:unit'
//include ':test:lib'
//include ':test:superLib'
//include ':sample'
//include ':test:remote'