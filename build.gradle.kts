plugins {
    id("io.papermc.paperweight.userdev") version "1.2.0" apply false
    kotlin("jvm") version "1.6.0" apply false
}

allprojects {

    group = "com.kitsune.vivid"
    version = "1.1-SNAPSHOT"


    repositories {

        // central
        mavenCentral()

        // paper repo
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
