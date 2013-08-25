package org.gozer.resources;

import org.gozer.model.Project;
import org.gozer.repositories.ProjectRepository;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Set;

@Component
@RestxResource
public class ProjectResource {

    private ProjectRepository projectRepository;

    public ProjectResource(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }


    @PermitAll
    @GET("/projects")
    public Set<Project> getAll() {
        return projectRepository.getAll();
    }

    @PermitAll
    @POST("/projects")
    public Project create(Project project) {
        return projectRepository.create(project);
    }
}