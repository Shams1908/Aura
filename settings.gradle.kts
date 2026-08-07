pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "Aura"

// Core Modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:security")
include(":core:designsystem")
include(":core:vision")

// Feature Modules
include(":feature:auth")
include(":feature:home")
include(":feature:detail")
include(":feature:camera")
include(":feature:ai")
include(":feature:rendering")
include(":feature:shopping")
include(":feature:profile")

include(":app")

