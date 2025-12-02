package fr.traqueur.bootstrap;

import fr.traqueur.bootstrap.config.DependencyManifest;
import fr.traqueur.bootstrap.loader.IsolatedClassLoader;
import fr.traqueur.bootstrap.loader.LoaderContext;
import fr.traqueur.bootstrap.resolver.ArtifactResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point for the dynamic loading system.
 * This class provides static methods to bootstrap applications with dynamic dependencies.
 *
 * <p>The bootstrap process:</p>
 * <ol>
 *   <li>Loads the dependency manifest from META-INF/dynamic-dependencies.json</li>
 *   <li>Resolves and downloads dependencies using Maven Resolver</li>
 *   <li>Creates an isolated child-first ClassLoader with the dependencies</li>
 *   <li>Loads and instantiates the application class within the isolated ClassLoader</li>
 *   <li>Calls the application's start method</li>
 * </ol>
 *
 * <p>Simple usage:</p>
 * <pre>{@code
 * public class Main {
 *     public static void main(String[] args) {
 *         DynamicLoader.bootstrap(args, MyApp.class);
 *     }
 * }
 * }</pre>
 *
 * <p>Advanced callback usage:</p>
 * <pre>{@code
 * DynamicLoader.bootstrap(args, ctx -> {
 *     MyApp app = ctx.create(MyApp.class);
 *     app.start(ctx.args());
 * });
 * }</pre>
 */
public final class BootstrapLoader {

    private static final String MANIFEST_PATH = "META-INF/bootstrap-dependencies.json";
    private static final String CACHE_DIR_PROPERTY = "bootstraploader.cache.dir";
    private static final String CACHE_DIR_ENV = "BOOTSTRAP_LOADER_CACHE_DIR";
    private static final String DEFAULT_CACHE_DIR = ".bootstrap-loader/cache";

    private BootstrapLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Bootstraps the application using the simple interface style with a class name.
     * This method loads dynamic dependencies and creates an instance of the specified application class.
     *
     * <p><b>Recommended:</b> Use this method instead of the Class variant when your application class
     * references types from dynamic dependencies (e.g., extends a class from a dynamic dependency).</p>
     *
     * @param args the command line arguments
     * @param applicationClassName the fully qualified name of the application class implementing {@link BootstrapApplication}
     * @throws RuntimeException if bootstrap fails
     */
    public static void bootstrap(String[] args, String applicationClassName) {
        bootstrap(args, ctx -> {
            Class<?> clazz = ctx.loadClass(applicationClassName);
            BootstrapApplication app = (BootstrapApplication) clazz.getDeclaredConstructor().newInstance();
            app.start(ctx.args());
        });
    }

    /**
     * Bootstraps the application using the simple interface style.
     * This method loads dynamic dependencies and creates an instance of the specified application class.
     *
     * <p><b>Warning:</b> If your application class references types from dynamic dependencies
     * (e.g., extends or implements a class from a dynamic dependency), use
     * {@link #bootstrap(String[], String)} instead to avoid ClassNotFoundException.</p>
     *
     * @param args the command line arguments
     * @param applicationClass the application class implementing {@link BootstrapApplication}
     * @throws RuntimeException if bootstrap fails
     */
    public static void bootstrap(String[] args, Class<? extends BootstrapApplication> applicationClass) {
        bootstrap(args, applicationClass.getName());
    }

    /**
     * Bootstraps the application using the advanced callback style.
     * This method gives more control over the initialization process.
     *
     * @param args the command line arguments
     * @param entrypoint the entrypoint callback
     * @throws RuntimeException if bootstrap fails
     */
    public static void bootstrap(String[] args, BootstrapEntrypoint entrypoint) {
        try {
            System.out.println("[Bootstrap] Starting bootstrap process...");

            // Load manifest
            DependencyManifest manifest = loadManifest();
            System.out.println("[Bootstrap] Loaded manifest with " + manifest.dependencies().size() + " dependencies");

            // Resolve dependencies
            Path cacheDir = getCacheDirectory();
            System.out.println("[Bootstrap] Using cache directory: " + cacheDir);

            ArtifactResolver resolver = new ArtifactResolver(cacheDir, manifest.repositories());
            List<Path> artifacts = resolver.resolve(manifest.dependencies());
            System.out.println("[Bootstrap] Resolved " + artifacts.size() + " artifacts");

            // Add the application JAR itself to the classpath
            // This is necessary so that application classes (like DiscordBot) are loaded
            // by the IsolatedClassLoader which has access to dynamic dependencies
            Path appJar = getApplicationJar();
            if (appJar != null) {
                artifacts.addFirst(appJar);  // Add at beginning to prioritize app classes
                System.out.println("[Bootstrap] Added application JAR: " + appJar.getFileName());
            }

            // Create isolated ClassLoader
            IsolatedClassLoader classLoader = new IsolatedClassLoader(artifacts);
            LoaderContext context = new LoaderContext(classLoader, args);

            System.out.println("[Bootstrap] ClassLoader ready, running entrypoint...");

            // Run entrypoint
            entrypoint.run(context);

        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap application", e);
        }
    }

    /**
     * Loads the dependency manifest from the classpath.
     *
     * @return the parsed manifest
     * @throws IOException if the manifest cannot be loaded or parsed
     */
    private static DependencyManifest loadManifest() throws IOException {
        ClassLoader classLoader = BootstrapLoader.class.getClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(MANIFEST_PATH)) {
            if (is == null) {
                throw new IOException("Manifest not found: " + MANIFEST_PATH);
            }
            String json = new String(is.readAllBytes());
            return DependencyManifest.parse(json);
        }
    }

    /**
     * Gets the cache directory for downloaded artifacts.
     * Priority: system property > environment variable > default.
     *
     * @return the cache directory path
     */
    private static Path getCacheDirectory() {
        String cacheDir = System.getProperty(CACHE_DIR_PROPERTY);
        if (cacheDir == null) {
            cacheDir = System.getenv(CACHE_DIR_ENV);
        }
        if (cacheDir == null) {
            cacheDir = DEFAULT_CACHE_DIR;
        }
        return Paths.get(cacheDir);
    }

    /**
     * Gets the path to the application JAR file.
     *
     * @return the path to the JAR, or null if not running from a JAR
     */
    private static Path getApplicationJar() {
        try {
            var location = BootstrapLoader.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path path = Paths.get(location.toURI());
                // Only return if it's a JAR file
                if (path.toString().endsWith(".jar")) {
                    return path;
                }
            }
        } catch (Exception e) {
            System.err.println("[Bootstrap] Warning: Could not determine application JAR location: " + e.getMessage());
        }
        return null;
    }
}