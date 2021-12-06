plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

repositories {

    // npc-lib repo
    maven("https://jitpack.io")
}

dependencies {

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")

    // paper
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    // npc-lib, thanks juliarn!
    implementation("com.github.juliarn:npc-lib:development-SNAPSHOT")

    // test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {

    build {
        dependsOn("shadowJar")
    }

    test {
        useJUnitPlatform()
    }
}