package fr.traqueur.bootstrap.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task that generates the dynamic dependencies manifest JSON file.
 * This file is placed in META-INF/dynamic-dependencies.json and contains
 * the list of dependencies to be loaded at runtime.
 */
abstract class GenerateDynamicManifestTask : DefaultTask() {

    /**
     * The dynamic configuration containing dependencies to include in the manifest.
     */
    @get:Internal
    abstract val dynamicConfiguration: Property<Configuration>

    /**
     * The project repositories to include in the manifest.
     */
    @get:Internal
    abstract val repositories: ListProperty<ArtifactRepository>

    /**
     * The output directory where the manifest will be generated.
     */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /**
     * Input property for up-to-date checking based on dependencies.
     */
    @get:Input
    val dependencyCoordinates: Provider<List<String>>
        get() = dynamicConfiguration.map { config ->
            config.allDependencies
                .filter { it.group != null && it.version != null }
                .map { "${it.group}:${it.name}:${it.version}" }
                .sorted()
        }

    /**
     * Input property for up-to-date checking based on repositories.
     */
    @get:Input
    val repositoryUrls: Provider<List<String>>
        get() = repositories.map { repos ->
            repos.filterIsInstance<MavenArtifactRepository>()
                .mapNotNull { it.url.toString() }
                .sorted()
        }

    @TaskAction
    fun generate() {
        val config = dynamicConfiguration.get()
        val repos = repositories.get()

        // Collect dependencies
        val dependencies = config.allDependencies
            .filter { it.group != null && it.version != null }
            .map { """"${it.group}:${it.name}:${it.version}"""" }
            .toList()

        // Collect repositories
        val repositoriesList = repos.filterIsInstance<MavenArtifactRepository>()
            .map { repo ->
                val id = repo.name
                val url = repo.url.toString()
                """    { "id": "$id", "url": "$url" }"""
            }
            .toList()

        // Generate JSON
        val json = buildString {
            appendLine("{")
            appendLine("""  "dependencies": [""")
            appendLine(dependencies.joinToString(",\n") { "    $it" })
            appendLine("  ],")
            appendLine("""  "repositories": [""")
            appendLine(repositoriesList.joinToString(",\n"))
            appendLine("  ]")
            appendLine("}")
        }

        // Write to file
        val outputDir = outputDirectory.get().asFile
        val metaInfDir = File(outputDir, "META-INF")
        metaInfDir.mkdirs()

        val manifestFile = File(metaInfDir, "bootstrap-dependencies.json")
        manifestFile.writeText(json)

        logger.lifecycle("Generated dynamic dependencies manifest: ${manifestFile.absolutePath}")
        logger.lifecycle("Dynamic dependencies: ${dependencies.size}")
    }
}