package org.gozer.resources;

import org.gozer.model.Project;
import org.gozer.repositories.ProjectRepository;
import org.gozer.services.CompilationService;
import org.gozer.services.DependenciesService;
import org.gozer.services.DeploiementService;
import org.gozer.services.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.annotations.*;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
@RestxResource
public class ProjectResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectResource.class);
    private DependenciesService dependenciesService;
    private ProjectRepository projectRepository;
    private GitService gitService;
    private CompilationService compilationService;
    private DeploiementService deploiementService;

    public ProjectResource(ProjectRepository projectRepository,
                           DependenciesService dependenciesService,
                           GitService gitService,
                           CompilationService compilationService,
                           DeploiementService deploiementService) {
        this.projectRepository = projectRepository;
        this.dependenciesService = dependenciesService;
        this.gitService = gitService;
        this.compilationService = compilationService;
        this.deploiementService = deploiementService;
    }


    @PermitAll
    @GET("/projects")
    public Collection<Project> getAll() {
        return projectRepository.getAll();
    }

    @PermitAll
    @POST("/projects")
    public Project create(Project project) {
        LOGGER.info("Create project : {}", project.getName());
        LOGGER.debug("project information : {}", project);
        return projectRepository.create(project);
    }

    @PermitAll
    @DELETE("/projects/{projectName}")
    public boolean delete(String projectName) {
        return projectRepository.delete(projectName);
    }

    @PermitAll
    @PUT("/projects/{projectName}/resolve")
    public Project resolve(String projectName) {

        checkNotNull(projectName); // TODO ça ne peut pas arriver
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            return null;
        }

        LOGGER.debug("Try to resolve the project {}", project.getName());

        project = dependenciesService.resolve(project);

        projectRepository.update(project);

        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/deploy")
    public Project deploy(String projectName) {

        checkNotNull(projectName);
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            return null;
        }

        project = deploiementService.deploy(project);

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
        LOGGER.debug("Try to clone the project {}", project.getName());

        project = gitService.cloneRepository(project);

        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/compile")
    public Project compile(String projectName) {

        checkNotNull(projectName);
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            return null;
        }

        project = compilationService.build(project);

        return project;
    }

}