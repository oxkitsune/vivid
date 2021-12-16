import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
}

dependencies {
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
    name = "Example"
    main = "com.kitsune.example.ExamplePlugin"
    apiVersion = "1.13"
}