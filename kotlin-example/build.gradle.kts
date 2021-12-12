import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
}

dependencies {

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")

    // paper
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    // vivid
    implementation(project(":core"))
}

tasks.build {
    dependsOn("shadowJar")
    dependsOn("reobfJar")
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "com.kitsune.example.ExamplePlugin"
    apiVersion = "1.13"
}