package fr.traqueur.bootstrap;

/**
 * Interface to be implemented by the main application class.
 * This interface provides a single entry point for applications that use dynamic loading.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * public class MyApp implements DynamicApplication {
 *     private JDA jda;
 *
 *     @Override
 *     public void start(String[] args) throws Exception {
 *         jda = JDABuilder.createDefault(token).build();
 *     }
 * }
 * }</pre>
 *
 * @see BootstrapLoader#bootstrap(String[], Class)
 */
public interface BootstrapApplication {

    /**
     * Called after all dynamic dependencies have been loaded and the isolated ClassLoader is ready.
     *
     * @param args the command line arguments passed to the application
     * @throws Exception if an error occurs during application startup
     */
    void start(String[] args) throws Exception;
}