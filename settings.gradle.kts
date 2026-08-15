pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PepLog"

// Entry Point
include(":app")

// Core Modules
include(":core:ui")
include(":core:database")
include(":core:common")
include(":core:model")
include(":core:datastore")
include(":core:billing")
include(":core:backup")
include(":core:analytics")

// Feature Modules
include(":feature:dashboard")
include(":feature:protocol")
include(":feature:log")
include(":feature:calculator")
include(":feature:encyclopedia")
include(":feature:pkcurves")
include(":feature:injection")
include(":feature:inventory")
include(":feature:progress")
include(":feature:health")
include(":feature:reports")
include(":feature:settings")
include(":feature:onboarding")
include(":feature:paywall")

