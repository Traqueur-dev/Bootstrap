# Bootstrap - Dynamic Dependency Loader

A Java library + Gradle plugin for dynamically loading Maven dependencies at runtime. Dramatically reduce your JAR size by downloading large dependencies on first launch.

## Features

- **Ultra-light JAR**: Reduce size from ~40MB to ~2MB by loading dependencies dynamically
- **Simple API**: One line in your `main()` is enough
- **Child-First ClassLoader**: Proper isolation of dynamic dependencies
- **Maven Resolver**: Reliable resolution via Apache Maven Resolver
- **No Java Agent**: No agent, no process restart required
- **Gradle Plugin**: Easy integration with existing projects

## Use Cases

Perfect for applications with large dependencies like:
- Discord bots using JDA
- Applications using heavy libraries (Guava, Apache Commons, etc.)
- Microservices with optional features

## Quick Start

### 1. Add the plugin to your project

**build.gradle.kts:**
```kotlin
plugins {
    java
    application
    id("fr.traqueur.bootstrap")
}

dependencies {
    // Core library
    implementation(project(":bootstrap-core"))

    // Mark dependencies as dynamic
    // They will be available at compile time but NOT included in the JAR
    dynamic("net.dv8tion:JDA:6.1.2")
    dynamic("com.google.guava:guava:32.1.3-jre")
}
```

### 2. Use the simple bootstrap API

**Main.java:**
```java
import fr.traqueur.bootstrap.BootstrapLoader;

public class Main {
    public static void main(String[] args) {
        DynamicLoader.bootstrap(args, MyApp.class);
    }
}
```

**MyApp.java:**
```java
import fr.traqueur.bootstrap.BootstrapApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class MyApp implements DynamicApplication {
    private JDA jda;  // You can use dynamic dependency types directly!

    @Override
    public void start(String[] args) throws Exception {
        jda = JDABuilder.createDefault(token).build();
        System.out.println("Bot started!");
    }
}
```

### 3. Build and run

```bash
./gradlew :example:jar
java -jar example/build/libs/example-1.0.0-SNAPSHOT.jar
```

On first run, dependencies will be downloaded to `.dynamic-loader/cache/`.

## How It Works

### Build Time

1. The Gradle plugin creates a `dynamic` configuration extending `compileOnly`
2. Dynamic dependencies are available during compilation
3. A manifest file `META-INF/dynamic-dependencies.json` is generated
4. Dynamic dependencies are **excluded** from the JAR

### Runtime

1. `DynamicLoader.bootstrap()` loads the manifest
2. Maven Resolver downloads dependencies and their transitive dependencies
3. An **IsolatedClassLoader** (child-first) is created with the dependencies
4. Your application class is loaded and instantiated via the isolated ClassLoader
5. Your `start()` method is called

### Child-First ClassLoader

The `IsolatedClassLoader` uses a **child-first** strategy (not Java's default parent-first):

1. System packages (`java.*`, `javax.*`, `sun.*`, `jdk.*`, `fr.traqueur.bootstrap.*`) → parent
2. Everything else: try child first, then delegate to parent

This is crucial for allowing your application classes to reference types from dynamic dependencies.

## Advanced Usage

### Callback Style

For more control over initialization:

```java
DynamicLoader.bootstrap(args, ctx -> {
    MyApp app = ctx.create(MyApp.class);

    // Access the ClassLoader
    ClassLoader cl = ctx.getClassLoader();

    // Load classes dynamically
    Class<?> someClass = ctx.loadClass("com.example.SomeClass");

    app.start(ctx.args());
});
```

### Custom Cache Directory

Via system property or environment variable:

```bash
# System property
java -Ddynamicloader.cache.dir=/custom/cache -jar app.jar

# Environment variable
export DYNAMIC_LOADER_CACHE_DIR=/custom/cache
java -jar app.jar
```

### Custom Repositories

The plugin automatically includes all repositories from your Gradle project:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.example.com/maven2")
    }
}
```

These repositories are included in the generated manifest.

## Architecture

```
Bootstrap/
├── bootstrap-core/               # Runtime library (Java 21)
│   ├── DynamicLoader.java        # Main entry point
│   ├── DynamicApplication.java   # Simple interface
│   ├── DynamicEntrypoint.java    # Callback interface
│   ├── config/
│   │   └── DependencyManifest.java   # JSON parser
│   ├── loader/
│   │   ├── IsolatedClassLoader.java  # Child-first ClassLoader
│   │   └── LoaderContext.java        # Context for callbacks
│   └── resolver/
│       ├── ArtifactResolver.java     # Maven Resolver integration
│       └── SimpleTransferListener.java
│
├── bootstrap-gradle/             # Gradle plugin (Kotlin)
│   ├── DynamicLoaderPlugin.kt
│   ├── DynamicLoaderExtension.kt
│   └── GenerateDynamicManifestTask.kt
│
└── example/                      # Discord bot example
```

## Manifest Format

**META-INF/dynamic-dependencies.json:**
```json
{
  "dependencies": [
    "net.dv8tion:JDA:6.1.2",
    "com.google.guava:guava:32.1.3-jre"
  ],
  "repositories": [
    { "id": "MavenRepo", "url": "https://repo.maven.apache.org/maven2/" }
  ]
}
```

## Requirements

- Java 21 or higher
- Gradle 8.x
- Accessible Maven repositories

## Example Project

See the `example/` directory for a complete Discord bot implementation using JDA.

### Running the Example

```bash
# Build the project
./gradlew build

# Run the example (with Discord token)
java -jar example/build/libs/example-1.0.0-SNAPSHOT.jar YOUR_DISCORD_TOKEN

# Or set environment variable
export DISCORD_TOKEN=your_token_here
java -jar example/build/libs/example-1.0.0-SNAPSHOT.jar
```

## Publishing

To publish to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

To use in other projects:

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("fr.traqueur.bootstrap:bootstrap-core:1.0.0-SNAPSHOT")
}
```

## Project Configuration

The plugin is configured as a composite build in `settings.gradle.kts`:

```kotlin
includeBuild("bootstrap-gradle")  // Plugin available for local development
include("bootstrap-core")
include("example")
```

## Limitations

- No Java agent support (by design)
- Internet connection required on first run
- Dependencies must be available in configured Maven repositories
- Minimal JSON parser (no external JSON library)

## Contributing

Contributions are welcome! Please ensure:
- Java 21 compatibility
- Proper Javadoc documentation
- Clean code structure
- Avoid unnecessary dependencies

## License

MIT License - feel free to use in your projects!

## Credits

Built with:
- Apache Maven Resolver
- Gradle Plugin API
- Kotlin DSL

---

**Questions?** Open an issue on GitHub!