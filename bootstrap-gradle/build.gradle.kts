import java.util.Locale

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = rootProject.group
version = rootProject.version

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
    var repository = System.getProperty("repository.name", "snapshots").replaceFirstChar { if (it.isLowerCase()) it.titlecase(
        Locale.getDefault()) else it.toString() }
    repositories {
        maven {
            name = "groupez${repository}"
            url = uri("https://repo.groupez.dev/${repository.lowercase()}")
            credentials {
                username = findProperty("${name}Username") as String? ?: System.getenv("MAVEN_USERNAME")
                password = findProperty("${name}Password") as String? ?: System.getenv("MAVEN_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            // Configure only the main plugin publication, not the marker
            named<MavenPublication>("pluginMaven") {
                groupId = project.group.toString()
                artifactId = project.name.lowercase()
                version = project.version.toString()

                pom {
                    scm {
                        connection = "scm:git:git://github.com/Traqueur-dev/${rootProject.name}.git"
                        developerConnection = "scm:git:ssh://github.com/Traqueur-dev/${rootProject.name}.git"
                        url = "https://github.com/Traqueur-dev/${rootProject.name}/"
                    }
                }
            }
        }
    }
}