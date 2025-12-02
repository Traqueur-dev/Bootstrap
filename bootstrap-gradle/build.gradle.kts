plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("dynamicLoader") {
            id = "fr.traqueur.bootstrap"
            implementationClass = "fr.traqueur.bootstrap.gradle.BootstrapLoaderPlugin"
            displayName = "Dynamic Loader Plugin"
            description = "Gradle plugin for dynamic dependency loading at runtime"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}