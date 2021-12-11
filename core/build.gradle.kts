plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka") version "1.6.0"
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

publishing {

    publications {

        create<MavenPublication>("maven") {

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }

    }

}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-doc")
}



tasks {

    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "16"
    }

    reobfJar {

        // set the output jar's final name.
        // this is not ideal, but it's the only way right now using paperweight
        outputJar.set(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
    }

    java {
        withSourcesJar()
    }

    // add javadoc jars
    artifacts {
        add("archives", dokkaJavadocJar)
        add("archives", dokkaHtmlJar)
    }

    build {
        dependsOn("kotlinSourcesJar")
        dependsOn("shadowJar")
    }

    test {
        useJUnitPlatform()
    }
}
