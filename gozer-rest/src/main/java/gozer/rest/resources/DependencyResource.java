package gozer.rest.resources;

import gozer.model.Dependency;
import gozer.model.builders.DependencyBuilder;
import gozer.repositories.DependencyRepository;
import gozer.services.DependenciesService;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Set;

@Component
@RestxResource
public class DependencyResource {

    private DependenciesService dependenciesService;
    private DependencyRepository dependencyRepository;

    public DependencyResource(DependencyRepository dependencyRepository, DependenciesService dependenciesService) {
        this.dependencyRepository = dependencyRepository;
        this.dependenciesService = dependenciesService;
    }

    @PermitAll
    @GET("/dependencies")
    public Set<Dependency> getAll() {
        return dependencyRepository.getAll();
    }

    @PermitAll
    @POST("/dependencies")
    public Dependency create(Dependency dependency) {
        return dependencyRepository.create(dependency);
    }

    @PermitAll
    @GET("/dependencies/{groupId}/{artifactId}/{version}/resolve")
    public Dependency resolve(String groupId, String artifactId, String version) {
        Dependency dependency = DependencyBuilder.aDependency().withGroupId(groupId)
                                             .withArtifactId(artifactId)
                                             .withVersion(version)
                                             .build();
        dependenciesService.resolve(dependency);
        return dependency;
    }
}