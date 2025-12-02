package fr.traqueur.bootstrap.resolver;

import fr.traqueur.bootstrap.config.DependencyManifest.Repository;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.supplier.RepositorySystemSupplier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves Maven dependencies using Apache Maven Resolver.
 * This class handles downloading artifacts and their transitive dependencies.
 */
public class ArtifactResolver {

    private final RepositorySystem repositorySystem;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> repositories;

    /**
     * Creates a new artifact resolver.
     *
     * @param localRepositoryPath the local cache directory for downloaded artifacts
     * @param repositories the list of remote Maven repositories to use
     */
    public ArtifactResolver(Path localRepositoryPath, List<Repository> repositories) {
        // Create repository system using supplier
        this.repositorySystem = new RepositorySystemSupplier().get();

        // Create session
        this.session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(localRepositoryPath.toFile());
        this.session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
        this.session.setTransferListener(new SimpleTransferListener());
        this.session.setReadOnly();

        // Convert repositories
        this.repositories = repositories.stream()
            .map(repo -> new RemoteRepository.Builder(repo.id(), "default", repo.url()).build())
            .collect(Collectors.toList());

        // Add Maven Central if no repositories specified
        if (this.repositories.isEmpty()) {
            this.repositories.add(
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()
            );
        }
    }

    /**
     * Resolves a list of dependencies and returns the paths to their JAR files.
     * This includes transitive dependencies.
     *
     * @param dependencyCoordinates the dependencies in format "groupId:artifactId:version"
     * @return the list of paths to resolved JAR files
     * @throws DependencyResolutionException if resolution fails
     */
    public List<Path> resolve(List<String> dependencyCoordinates) throws DependencyResolutionException {
        List<Dependency> dependencies = new ArrayList<>();

        for (String coords : dependencyCoordinates) {
            Artifact artifact = new DefaultArtifact(coords);
            dependencies.add(new Dependency(artifact, "compile"));
        }

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setDependencies(dependencies);
        collectRequest.setRepositories(repositories);

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setCollectRequest(collectRequest);

        List<ArtifactResult> results = repositorySystem.resolveDependencies(session, dependencyRequest)
            .getArtifactResults();

        return results.stream()
            .map(result -> result.getArtifact().getFile().toPath())
            .collect(Collectors.toList());
    }
}