rootProject.name = "vivid"

pluginManagement {
    repositories {

        // kotlin/shadow plugin
        gradlePluginPortal()

        // paperweight
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

include("core", "kotlin-example")
include("java-example")
