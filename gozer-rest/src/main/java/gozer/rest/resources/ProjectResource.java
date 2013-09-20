package gozer.rest.resources;

import gozer.model.Project;
import gozer.repositories.ProjectRepository;
import gozer.rest.exceptions.UnknownProjectException;
import gozer.services.CompilationService;
import gozer.services.DependenciesService;
import gozer.services.DeploiementService;
import gozer.services.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.annotations.*;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.Collection;

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
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            LOGGER.warn("Project {} is unknown", projectName);
            throw new UnknownProjectException(projectName);
        }

        LOGGER.debug("Try to resolve the project {}", project.getName());

        project = dependenciesService.resolve(project);

        projectRepository.update(project);

        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/deploy")
    public Project deploy(String projectName) {

        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            LOGGER.warn("Project {} is unknown", projectName);
            throw new UnknownProjectException(projectName);
        }

        project = deploiementService.deploy(project);

        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/clone")
    public Project clone(String projectName) {
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            LOGGER.warn("Project {} is unknown", projectName);
            throw new UnknownProjectException(projectName);
        }
        LOGGER.debug("Try to clone the project {}", project.getName());

        project = gitService.clone(project);

        return project;
    }

    @PermitAll
    @PUT("/projects/{projectName}/compile")
    public Project compile(String projectName) {
        Project project = projectRepository.findByName(projectName);
        if (project == null) {
            LOGGER.warn("Project {} is unknown", projectName);
            throw new UnknownProjectException(projectName);
        }

        project = compilationService.build(project);

        return project;
    }

}