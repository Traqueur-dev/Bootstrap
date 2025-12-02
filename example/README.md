# Discord Bot Example

This example demonstrates the Dynamic Loader library with a simple Discord bot using JDA.

## Features

- Uses JDA as a dynamic dependency (not bundled in JAR)
- Simple command handler (`!ping`, `!info`)
- Demonstrates both simple and callback-style APIs

## Building

```bash
# From project root
./gradlew :example:build

# Create runnable JAR
./gradlew :example:jar
```

## Running

### Option 1: Pass token as argument

```bash
java -jar build/libs/example-1.0.0-SNAPSHOT.jar YOUR_DISCORD_TOKEN
```

### Option 2: Use environment variable

```bash
export DISCORD_TOKEN=your_token_here
java -jar build/libs/example-1.0.0-SNAPSHOT.jar
```

## First Run

On first run, you'll see:

```
[DynamicLoader] Starting bootstrap process...
[DynamicLoader] Loaded manifest with 2 dependencies
[DynamicLoader] Using cache directory: .dynamic-loader/cache
[Download] https://repo.maven.apache.org/.../JDA-5.0.0-beta.24.jar
[Progress] 1.5 MB / 15.2 MB (10.0%)
...
[Complete] JDA-5.0.0-beta.24.jar (15.2 MB)
[DynamicLoader] Resolved 23 artifacts
[DynamicLoader] ClassLoader ready, running entrypoint...
[Bot] Starting Discord bot...
[Bot] Bot is ready! Logged in as: YourBot#1234
```

Subsequent runs will use the cached dependencies and start instantly!

## JAR Size Comparison

- **Without Dynamic Loader**: ~40 MB (includes JDA and all dependencies)
- **With Dynamic Loader**: ~2 MB (only core library)
- **Cache directory**: ~16 MB (downloaded on first run)

## Commands

- `!ping` - Replies with "Pong! 🏓"
- `!info` - Shows information about the bot and Dynamic Loader

## Code Structure

### Simple Style (Main.java)

```java
public class Main {
    public static void main(String[] args) {
        DynamicLoader.bootstrap(args, DiscordBot.class);
    }
}
```

### Callback Style (CallbackStyleExample.java)

```java
public class CallbackStyleExample {
    public static void main(String[] args) {
        DynamicLoader.bootstrap(args, ctx -> {
            DiscordBot bot = ctx.create(DiscordBot.class);
            bot.start(ctx.args());
        });
    }
}
```

### Bot Implementation (DiscordBot.java)

Implements `DynamicApplication` and uses JDA types directly:

```java
public class DiscordBot implements DynamicApplication {
    private JDA jda;  // JDA is a dynamic dependency!

    @Override
    public void start(String[] args) throws Exception {
        jda = JDABuilder.createDefault(token).build();
    }
}
```

## Notes

- The bot requires `MESSAGE_CONTENT` intent
- Token must be a valid Discord bot token
- Internet connection required on first run for dependency download