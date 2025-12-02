plugins {
    java
    kotlin("jvm") version "2.3.0-RC" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = property("group")!!
    version = property("version")!!
}

tasks.register("publish") {
    dependsOn(":bootstrap-core:publish")
    dependsOn(gradle.includedBuild("bootstrap-gradle").task(":publish"))
}