package fr.traqueur.bootstrap.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

/**
 * Gradle plugin that enables dynamic dependency loading.
 *
 * This plugin:
 * - Creates a "dynamic" configuration that extends "compileOnly"
 * - Generates META-INF/dynamic-dependencies.json with dependency information
 * - Excludes dynamic dependencies from the runtime classpath
 *
 * Usage in build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("fr.traqueur.bootstrap") version "1.0.0-SNAPSHOT"
 * }
 *
 * dependencies {
 *     dynamic("net.dv8tion:JDA:5.0.0-beta.24")
 * }
 * ```
 */
class BootstrapLoaderPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply Java plugin if not already applied
        project.plugins.apply(JavaPlugin::class.java)

        // Create extension
        project.extensions.create("bootstrapLoader", BootstrapLoaderExtension::class.java)

        // Create dynamic configuration
        val dynamicConfig = createDynamicConfiguration(project)

        // Get source sets
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        // Register manifest generation task
        val generateTask = project.tasks.register("generateDynamicManifest", GenerateDynamicManifestTask::class.java)

        generateTask.configure {
            group = "build"
            description = "Generates the dynamic dependencies manifest"
            dynamicConfiguration.set(dynamicConfig)
            repositories.set(project.repositories.toList())

            // Output directory
            val outputDir = project.layout.buildDirectory.dir("generated/resources/bootstrap-loader")
            outputDirectory.set(outputDir)

            // Add generated resources to main source set
            mainSourceSet.resources.srcDir(outputDir)
        }

        // Make processResources depend on generateDynamicManifest
        project.tasks.named("processResources").configure {
            dependsOn(generateTask)
        }
    }

    /**
     * Creates the "dynamic" configuration that extends "compileOnly".
     * Dependencies in this configuration are available at compile time but not in the runtime classpath.
     */
    private fun createDynamicConfiguration(project: Project): Configuration {
        val compileOnly = project.configurations.getByName("compileOnly")

        val dynamicConfig = project.configurations.create("bootstrap").apply {
            isTransitive = true
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        // Make compileOnly extend from dynamic so dynamic dependencies are available at compile time
        compileOnly.extendsFrom(dynamicConfig)

        return dynamicConfig
    }
}