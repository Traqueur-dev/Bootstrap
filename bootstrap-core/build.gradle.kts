import java.util.Locale

plugins {
    java
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // Maven Resolver for dependency resolution
    implementation("org.apache.maven.resolver:maven-resolver-api:1.9.18")
    implementation("org.apache.maven.resolver:maven-resolver-impl:1.9.18")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:1.9.18")
    implementation("org.apache.maven.resolver:maven-resolver-transport-http:1.9.18")
    implementation("org.apache.maven.resolver:maven-resolver-supplier:1.9.18")
    implementation("org.apache.maven:maven-resolver-provider:3.9.6")
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

    publications {
        register<MavenPublication>("groupez${repository}") {
            pom {
                groupId = project.group as String?
                name = project.name
                artifactId = name.get().lowercase()
                version = project.version as String?

                scm {
                    connection = "scm:git:git://github.com/Traqueur-dev/${rootProject.name}.git"
                    developerConnection = "scm:git:ssh://github.com/Traqueur-dev/${rootProject.name}.git"
                    url = "https://github.com/Traqueur-dev/${rootProject.name}/"
                }
            }
            from(components["java"])
        }
    }
}