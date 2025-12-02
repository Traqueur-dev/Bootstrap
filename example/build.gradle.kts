plugins {
    java
    application
    id("fr.traqueur.bootstrap")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":bootstrap-core"))

    // Dynamic dependencies - available at compile time but not in JAR
    // These will be downloaded at runtime on first launch
    bootstrap("net.dv8tion:JDA:6.1.2")
    bootstrap("org.slf4j:slf4j-simple:2.0.9")

    // Jackson annotations is required by Jackson databind but sometimes not resolved transitively
    bootstrap("com.fasterxml.jackson.core:jackson-annotations:2.20")
}

application {
    mainClass.set("fr.traqueur.bootstrap.example.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "fr.traqueur.bootstrap.example.Main"
    }
    // Create a fat JAR with only the core dependencies (not dynamic ones)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}