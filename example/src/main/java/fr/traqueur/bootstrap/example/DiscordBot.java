package fr.traqueur.bootstrap.example;

import fr.traqueur.bootstrap.BootstrapApplication;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * Example Discord bot using JDA.
 * Notice that we can use JDA types directly (like the jda field) even though
 * JDA is a dynamic dependency. This works because the IsolatedClassLoader
 * uses a child-first loading strategy.
 */
public class DiscordBot extends ListenerAdapter implements BootstrapApplication {

    private JDA jda;

    @Override
    public void start(String[] args) throws Exception {
        System.out.println("[Bot] Starting Discord bot...");

        // Get token from args or environment variable
        String token = getToken(args);
        if (token == null) {
            System.err.println("Usage: java -jar bot.jar <token>");
            System.err.println("   or: set DISCORD_TOKEN environment variable");
            System.exit(1);
        }

        // Build JDA instance
        jda = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .setActivity(Activity.playing("with dynamic loading!"))
            .addEventListeners(this)
            .build();

        System.out.println("[Bot] Waiting for JDA to be ready...");
        jda.awaitReady();
        System.out.println("[Bot] Bot is ready! Logged in as: " + jda.getSelfUser().getAsTag());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();

        // Simple ping command
        if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong! 🏓").queue();
        }

        // Info command showing jar size
        if (message.equalsIgnoreCase("!info")) {
            String response = """
                This bot uses Dynamic Loader!

                Benefits:
                ✅ Small JAR size (~2MB instead of ~40MB)
                ✅ Dependencies loaded on first run
                ✅ No agent, no process restart
                ✅ Simple one-line API
                """;
            event.getChannel().sendMessage(response).queue();
        }
    }

    /**
     * Gets the Discord token from args or environment variable.
     */
    private String getToken(String[] args) {
        if (args.length > 0) {
            return args[0];
        }
        return System.getenv("DISCORD_TOKEN");
    }
}