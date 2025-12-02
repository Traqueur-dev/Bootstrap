plugins {
    java
    kotlin("jvm") version "2.3.0-RC" apply false
}

group = "fr.traqueur.bootstrap"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks.register("publish") {
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("publish")
    })
    dependsOn(gradle.includedBuild("bootstrap-gradle").task(":publish"))
}