plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

dependencies {

    // kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")

    // paper
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")

    // test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {

    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "16"
    }

    register<Jar>("dokkaJavadocJar") {
        dependsOn(dokkaJavadoc)
        from(dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    // configure paperweight plugin
    reobfJar {

        // set the output jar's final name.
        // this is not ideal, but it's the only way right now using paperweight
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    }

    test {
        useJUnitPlatform()
    }
}

// publishing configuration
publishing {

    publications {

        create<MavenPublication>("maven") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // add javadoc/sources
            artifact(tasks["dokkaJavadocJar"])
            artifact(tasks.kotlinSourcesJar)

            // add final artifact
            artifact(tasks.reobfJar)
        }

    }

    repositories {

        maven {
            url = uri("https://maven.kitsune.software/repository/snapshots/")
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }

    }

}
