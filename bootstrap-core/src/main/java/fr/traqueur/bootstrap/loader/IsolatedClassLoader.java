package fr.traqueur.bootstrap.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

/**
 * A child-first ClassLoader that prioritizes loading classes from dynamic dependencies.
 * This is crucial for allowing application classes to reference types from dynamically loaded dependencies.
 *
 * <p>Loading strategy:</p>
 * <ol>
 *   <li>For system classes (java.*, javax.*, sun.*, jdk.*), delegate to parent</li>
 *   <li>Try to load from dynamic dependency JARs first (child-first)</li>
 *   <li>If not found, delegate to parent ClassLoader</li>
 * </ol>
 *
 * <p>This differs from the default parent-first strategy and is necessary because
 * application classes loaded by the AppClassLoader need to resolve types from
 * dynamic dependencies when they are used as field types or method parameters.</p>
 */
public class IsolatedClassLoader extends URLClassLoader {

    private static final String[] PARENT_FIRST_PACKAGES = {
        "java.",
        "javax.",
        "sun.",
        "jdk.",
        // Bootstrap library classes must be parent-first to avoid ClassCastException
        "fr.traqueur.bootstrap.config.",
        "fr.traqueur.bootstrap.loader.",
        "fr.traqueur.bootstrap.resolver.",
        "fr.traqueur.bootstrap.BootstrapApplication",
        "fr.traqueur.bootstrap.BootstrapEntrypoint",
        "fr.traqueur.bootstrap.BootstrapLoader"
    };

    /**
     * Creates a new isolated ClassLoader with the specified artifact paths.
     *
     * @param artifacts the paths to JAR files to include in this ClassLoader
     */
    public IsolatedClassLoader(List<Path> artifacts) {
        super(toUrls(artifacts), IsolatedClassLoader.class.getClassLoader());
    }

    /**
     * Converts file paths to URLs for the URLClassLoader.
     *
     * @param paths the file paths to convert
     * @return an array of URLs
     */
    private static URL[] toUrls(List<Path> paths) {
        return paths.stream()
            .map(path -> {
                try {
                    return path.toUri().toURL();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert path to URL: " + path, e);
                }
            })
            .toArray(URL[]::new);
    }

    /**
     * Loads a class using child-first strategy.
     * System packages are always delegated to parent first.
     *
     * @param name the binary name of the class
     * @param resolve if true then resolve the class
     * @return the resulting Class object
     * @throws ClassNotFoundException if the class could not be found
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if class is already loaded
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            if (resolve) {
                resolveClass(loadedClass);
            }
            return loadedClass;
        }

        // For system packages, always delegate to parent first
        if (isParentFirstPackage(name)) {
            return super.loadClass(name, resolve);
        }

        // Child-first: try to load from our URLs first
        try {
            loadedClass = findClass(name);
            if (resolve) {
                resolveClass(loadedClass);
            }
            return loadedClass;
        } catch (ClassNotFoundException e) {
            // Not found in our URLs, delegate to parent
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Checks if a class should be loaded parent-first.
     *
     * @param className the class name to check
     * @return true if the class should be loaded from parent first
     */
    private boolean isParentFirstPackage(String className) {
        for (String prefix : PARENT_FIRST_PACKAGES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}