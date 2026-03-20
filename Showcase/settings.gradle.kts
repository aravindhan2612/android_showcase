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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "AndroidShowcase"
include(":app")
include(":feature:material3expressive")
include(":feature:ndkexample")
include(":feature:chatexample")
include(":core:navigation")
include(":feature:home")
include(":feature:parallelapiexample")
include(":core:network")
include(":core:ui")
include(":core:common")
