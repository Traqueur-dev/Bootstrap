package fr.traqueur.bootstrap.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the dependency manifest loaded from META-INF/dynamic-dependencies.json.
 * This class uses a minimal regex-based JSON parser to avoid external dependencies.
 *
 * <p>Expected JSON format:</p>
 * <pre>{@code
 * {
 *   "dependencies": [
 *     "groupId:artifactId:version",
 *     "net.dv8tion:JDA:5.0.0-beta.24"
 *   ],
 *   "repositories": [
 *     { "id": "central", "url": "https://repo.maven.apache.org/maven2/" }
 *   ]
 * }
 * }</pre>
 */
public record DependencyManifest(List<String> dependencies, List<Repository> repositories) {

    /**
     * Parses a JSON string into a DependencyManifest.
     * Uses simple regex patterns to extract dependencies and repositories.
     *
     * @param json the JSON string to parse
     * @return the parsed manifest
     * @throws IllegalArgumentException if the JSON is malformed
     */
    public static DependencyManifest parse(String json) {
        List<String> dependencies = new ArrayList<>();
        List<Repository> repositories = new ArrayList<>();

        // Parse dependencies array
        Pattern depsArrayPattern = Pattern.compile("\"dependencies\"\\s*:\\s*\\[([^]]*)]");
        Matcher depsArrayMatcher = depsArrayPattern.matcher(json);
        if (depsArrayMatcher.find()) {
            String depsArray = depsArrayMatcher.group(1);
            Pattern depPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher depMatcher = depPattern.matcher(depsArray);
            while (depMatcher.find()) {
                dependencies.add(depMatcher.group(1));
            }
        }

        // Parse repositories array
        Pattern reposArrayPattern = Pattern.compile("\"repositories\"\\s*:\\s*\\[([^]]*)]");
        Matcher reposArrayMatcher = reposArrayPattern.matcher(json);
        if (reposArrayMatcher.find()) {
            String reposArray = reposArrayMatcher.group(1);
            // Match repository objects
            Pattern repoObjPattern = Pattern.compile("\\{([^}]*)}");
            Matcher repoObjMatcher = repoObjPattern.matcher(reposArray);
            while (repoObjMatcher.find()) {
                String repoObj = repoObjMatcher.group(1);

                Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
                Pattern urlPattern = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

                Matcher idMatcher = idPattern.matcher(repoObj);
                Matcher urlMatcher = urlPattern.matcher(repoObj);

                if (idMatcher.find() && urlMatcher.find()) {
                    String id = idMatcher.group(1);
                    String url = urlMatcher.group(1);
                    repositories.add(new Repository(id, url));
                }
            }
        }

        return new DependencyManifest(dependencies, repositories);
    }

    /**
     * Represents a Maven repository configuration.
     */
    public record Repository(String id, String url) {
    }
}