package fr.traqueur.bootstrap.loader;

import fr.traqueur.bootstrap.BootstrapApplication;
import fr.traqueur.bootstrap.BootstrapEntrypoint;

/**
 * Context object provided to {@link BootstrapEntrypoint} callbacks.
 * This context provides access to the isolated ClassLoader and utility methods.
 * @param classLoader the isolated ClassLoader containing dynamic dependencies
 * @param args        the command line arguments
 */
public record LoaderContext(IsolatedClassLoader classLoader, String[] args) {

    /**
     * Creates an instance of the specified class using the isolated ClassLoader.
     * This is necessary because using {@code new ClassName()} directly would use
     * the wrong ClassLoader and fail to resolve dynamic dependencies.
     *
     * <p>The class is loaded via the isolated ClassLoader and instantiated using
     * reflection with the no-args constructor.</p>
     *
     * @param clazz the class to instantiate
     * @param <T>   the type of the class
     * @return a new instance of the class
     * @throws RuntimeException if instantiation fails
     */
    public <T extends BootstrapApplication> T create(Class<T> clazz) {
        try {
            // Load class through isolated ClassLoader
            Class<BootstrapApplication> loadedClass = this.loadClass(clazz.getName());

            // Create instance using reflection
            BootstrapApplication instance = loadedClass.getDeclaredConstructor().newInstance();

            // Cast to expected type
            return clazz.cast(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }

    /**
     * Loads a class by name using the isolated ClassLoader.
     *
     * @param className the fully qualified class name
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public Class<BootstrapApplication> loadClass(String className) throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);
        if (BootstrapApplication.class.isAssignableFrom(clazz))
            return (Class<BootstrapApplication>) clazz;
        throw new ClassNotFoundException(className);
    }
}