plugins {
    java
    kotlin("jvm") version "2.3.0-RC" apply false
}

group = "fr.traqueur.bootstrap"
version = "1.0.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}