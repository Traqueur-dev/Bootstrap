package fr.traqueur.bootstrap.example;

import fr.traqueur.bootstrap.BootstrapLoader;

/**
 * Example application main class.
 * This demonstrates the simple one-line bootstrap API.
 */
public class Main {

    public static void main(String[] args) {
        // Bootstrap with dynamic dependency loading
        // Use class name as String to avoid loading DiscordBot before dynamic deps are available
        // This is necessary because DiscordBot extends ListenerAdapter (from JDA, a dynamic dependency)
        BootstrapLoader.bootstrap(args, "fr.traqueur.bootstrap.example.DiscordBot");
    }
}