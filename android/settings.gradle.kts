pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://artifactory.estsoft.com/artifactory/libs-release/")
        }
    }
}

rootProject.name = "perso-live-sdk-kmp-sample"
include(":app")
