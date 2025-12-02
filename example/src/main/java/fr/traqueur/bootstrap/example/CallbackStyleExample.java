package fr.traqueur.bootstrap.example;

import fr.traqueur.bootstrap.BootstrapApplication;
import fr.traqueur.bootstrap.BootstrapLoader;

/**
 * Alternative example showing the callback-style API.
 * This gives more control over the initialization process.
 */
public class CallbackStyleExample {

    public static void main(String[] args) {
        // Advanced callback style
        BootstrapLoader.bootstrap(args, ctx -> {
            System.out.println("[Callback] ClassLoader ready!");
            System.out.println("[Callback] Args: " + String.join(", ", ctx.args()));

            // Load and create bot instance using the isolated ClassLoader
            Class<?> botClass = ctx.loadClass("fr.traqueur.bootstrap.example.DiscordBot");
            BootstrapApplication bot = (BootstrapApplication) botClass.getDeclaredConstructor().newInstance();

            // Start the bot
            bot.start(ctx.args());
        });
    }
}