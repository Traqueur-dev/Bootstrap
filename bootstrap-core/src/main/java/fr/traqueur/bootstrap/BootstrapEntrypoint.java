package fr.traqueur.bootstrap;

import fr.traqueur.bootstrap.loader.LoaderContext;

/**
 * Functional interface for advanced bootstrap callback style.
 * This allows for more control over the initialization process.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * DynamicLoader.bootstrap(args, ctx -> {
 *     MyApp app = ctx.create(MyApp.class);
 *     app.start(ctx.args());
 * });
 * }</pre>
 *
 * @see BootstrapLoader#bootstrap(String[], BootstrapEntrypoint)
 */
@FunctionalInterface
public interface BootstrapEntrypoint {

    /**
     * Called after all dynamic dependencies have been loaded.
     *
     * @param context the loader context containing the isolated ClassLoader and utilities
     * @throws Exception if an error occurs during execution
     */
    void run(LoaderContext context) throws Exception;
}