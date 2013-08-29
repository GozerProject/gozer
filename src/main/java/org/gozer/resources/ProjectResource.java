package org.gozer.resources;

import org.gozer.model.Dependency;
import org.gozer.model.Project;
import org.gozer.repositories.ProjectRepository;
import org.gozer.services.DependenciesService;
import org.gozer.services.GitService;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.gozer.model.Project.Status.DEPLOYED;

@Component
@RestxResource
public class ProjectResource {

    private DependenciesService dependenciesService;
    private ProjectRepository projectRepository;
    private GitService gitService;

    public ProjectResource(ProjectRepository projectRepository,
                           DependenciesService dependenciesService,
                           GitService gitService) {
        this.projectRepository = projectRepository;
        this.dependenciesService = dependenciesService;
        this.gitService = gitService;
    }


    @PermitAll
    @GET("/projects")
    public Collection<Project> getAll() {
        return projectRepository.getAll();
    }

    @PermitAll
    @POST("/projects")
    public Project create(Project project) {
        return projectRepository.create(project);
    }

    @PermitAll
    @PUT("/projects/{projectName}/deploy")
    public Project deploy(String projectName) {

        checkNotNull(projectName);
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            return null;
        }
        for (Dependency dependency : project.getDependencies().getCompile()) {
            dependenciesService.resolve(dependency);
        }
        project.setStatus(DEPLOYED);
        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/clone")
    public Project clone(String projectName) {

        checkNotNull(projectName);
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            return null;
        }

        project = gitService.cloneRepository(project);

        return project;
    }

}