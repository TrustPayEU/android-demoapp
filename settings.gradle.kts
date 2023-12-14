pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

sourceControl {
    gitRepository(java.net.URI("https://github.com/TrustPayEU/android-sdk.git")) {
        producesModule("sk.trustpay.api:sdk")
    }
}

rootProject.name = "TrustPayDemoApp"
include(":app")
 