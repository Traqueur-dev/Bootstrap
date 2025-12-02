# Bootstrap Usage Guide

## Quick Start

### Basic Usage (Recommended)

When your application class extends or implements classes from dynamic dependencies, use the **String-based** bootstrap:

```java
public class Main {
    public static void main(String[] args) {
        // Use class name as String - prevents early loading
        DynamicLoader.bootstrap(args, "com.example.MyApp");
    }
}
```

### Why String vs Class?

**Problem:** When you pass `MyApp.class` directly:
```java
DynamicLoader.bootstrap(args, MyApp.class);  // ❌ Can cause ClassNotFoundException
```

The JVM tries to load `MyApp` class **before** entering the bootstrap method. If `MyApp` extends/implements a class from a dynamic dependency (like JDA's `ListenerAdapter`), it will fail with `ClassNotFoundException` because the dynamic dependencies haven't been loaded yet.

**Solution:** Pass the class name as a String:
```java
DynamicLoader.bootstrap(args, "com.example.MyApp");  // ✅ Works correctly
```

The class is loaded **after** the dynamic dependencies are available via the `IsolatedClassLoader`.

## Complete Example

### build.gradle.kts
```kotlin
plugins {
    java
    application
    id("fr.traqueur.bootstrap")
}

dependencies {
    implementation(project(":bootstrap-core"))

    // Mark large dependencies as dynamic
    dynamic("net.dv8tion:JDA:6.1.2")
    dynamic("org.slf4j:slf4j-simple:2.0.9")
}

application {
    mainClass.set("com.example.Main")
}
```

### Main.java
```java
package com.example;

import fr.traqueur.bootstrap.BootstrapLoader;

public class Main {
    public static void main(String[] args) {
        // IMPORTANT: Use String, not MyApp.class
        DynamicLoader.bootstrap(args, "com.example.MyApp");
    }
}
```

### MyApp.java
```java
package com.example;

import fr.traqueur.bootstrap.BootstrapApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// Can extend classes from dynamic dependencies!
public class MyApp extends ListenerAdapter implements DynamicApplication {
    private JDA jda;

    @Override
    public void start(String[] args) throws Exception {
        String token = args.length > 0 ? args[0] : System.getenv("DISCORD_TOKEN");

        jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .build()
            .awaitReady();

        System.out.println("Bot ready: " + jda.getSelfUser().getAsTag());
    }
}
```

## Advanced: Callback Style

For more control, use the callback API:

```java
public class Main {
    public static void main(String[] args) {
        DynamicLoader.bootstrap(args, ctx -> {
            // Load class by name
            Class<?> appClass = ctx.loadClass("com.example.MyApp");

            // Create instance
            DynamicApplication app = (DynamicApplication)
                appClass.getDeclaredConstructor().newInstance();

            // Start application
            app.start(ctx.args());
        });
    }
}
```

## Build & Run

```bash
# Build project
./gradlew build

# Run (on first launch, dependencies will be downloaded)
java -jar example/build/libs/example-1.0.0-SNAPSHOT.jar YOUR_TOKEN

# Subsequent runs will use cached dependencies
java -jar example/build/libs/example-1.0.0-SNAPSHOT.jar YOUR_TOKEN
```

## First Launch Output

```
[DynamicLoader] Starting bootstrap process...
[DynamicLoader] Loaded manifest with 2 dependencies
[DynamicLoader] Using cache directory: .dynamic-loader/cache
[Download] https://repo.maven.apache.org/.../JDA-6.1.2.jar
[Progress] 2.5 MB / 15.8 MB (15.8%)
...
[Complete] JDA-6.1.2.jar (15.8 MB)
[DynamicLoader] Resolved 23 artifacts
[DynamicLoader] ClassLoader ready, running entrypoint...
[Bot] Starting Discord bot...
[Bot] Bot is ready! Logged in as: YourBot#1234
```

## Custom Cache Directory

```bash
# Via system property
java -Ddynamicloader.cache.dir=/custom/cache -jar app.jar

# Via environment variable
export DYNAMIC_LOADER_CACHE_DIR=/custom/cache
java -jar app.jar
```

## Troubleshooting

### ClassNotFoundException at startup

**Cause:** Passing `Class` instead of `String` when the class references dynamic dependencies.

**Fix:** Use String-based bootstrap:
```java
// ❌ Wrong - causes ClassNotFoundException
DynamicLoader.bootstrap(args, MyApp.class);

// ✅ Correct - class loaded after dependencies
DynamicLoader.bootstrap(args, "com.example.MyApp");
```

### NoClassDefFoundError inside application

**Cause:** Dependency not marked as `dynamic` in build.gradle.kts

**Fix:** Add to build.gradle.kts:
```kotlin
dependencies {
    dynamic("missing:dependency:1.0.0")
}
```

### Manifest not found

**Cause:** Plugin not applied or task not run

**Fix:** Ensure plugin is applied:
```kotlin
plugins {
    id("fr.traqueur.bootstrap")
}
```

## How It Works

1. **Build Time:**
   - Plugin creates `dynamic` configuration
   - Manifest generated in `META-INF/dynamic-dependencies.json`
   - Dynamic deps excluded from JAR

2. **Runtime:**
   - `DynamicLoader.bootstrap()` reads manifest
   - Maven Resolver downloads dependencies to cache
   - `IsolatedClassLoader` (child-first) created with deps
   - Application class loaded via isolated ClassLoader
   - `start()` method called with access to dynamic deps

## Benefits

- **Small JAR:** ~4MB instead of ~20MB+ with all dependencies
- **Fast Updates:** Only core changes require redistributing JAR
- **Flexible:** Different dependency versions per environment
- **Simple:** One-line API, no agent, no restart

---

For more details, see [README.md](README.md)