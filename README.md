# vivid
 a framework to build good-looking cut scenes in minecraft


## How to use

### Gradle

#### Kotlin/DSL:
```kotlin
repositories {
    maven("https://maven.kitsune.software/repositories/snapshots/")
}

implementation("com.kitsune.vivid:core:1.0-SNAPSHOT")
```

#### Groovy:
```groovy
repositories {
    maven {
        url = 'https://maven.kitsune.software/repositories/snapshots/'
    }
}

implementation 'com.kitsune.vivid:core:1.0-SNAPSHOT'
```

### Maven
```xml
<repository>
    <id>kitsune.software</id>
    <url>https://maven.kitsune.software/repositories/snapshots/'</url>
</repository>

<dependency>
    <groupId>com.kitsune.vivid</groupId>
    <artifactId>core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Example
Some `kotlin` examples can be found in [kotlin-example](kotlin-example)

Some `java` examples can be found in [java-example](java-example)
