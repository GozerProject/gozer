package org.gozer.resources;

import org.gozer.model.Dependencies;
import org.gozer.model.Dependency;
import org.gozer.services.DependenciesService;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import static org.gozer.builders.DependencyBuilder.aDependency;

@Component
@RestxResource
public class DependencyResource {
    @PermitAll
    @GET("/dependencies")
    public String getAll() {
        return "dependencies";
    }

    @PermitAll
    @POST("/dependencies")
    public String create(Dependencies dependencies) {
        return "dependencies";
    }

    @PermitAll
    @GET("/dependencies/{groupId}/{artifactId}/{version}/resolve")
    public String resolve(String groupId, String artifactId, String version) {
        DependenciesService dependenciesService = new DependenciesService();

        Dependency dependency = aDependency().withGroupId(groupId)
                                             .withArtifactId(artifactId)
                                             .withVersion(version)
                                             .build();
        dependenciesService.resolve(dependency);
        return "resolve";
    }
}