package gozer.services;

import gozer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

@Component
public class LifecycleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleService.class);
    private final GitService gitService;
    private final CompilationService compilationService;
    private final DependenciesService dependenciesService;

    public LifecycleService(GitService gitService,
                            CompilationService compilationService,
                            DependenciesService dependenciesService) {

        this.gitService = gitService;
        this.compilationService = compilationService;
        this.dependenciesService = dependenciesService;
    }

    public Project cycleUp(Project project) {
        switch (project.getStatus()) {
            case CREATED: return gitService.cloneRepository(project);
            case CLONED: return dependenciesService.resolve(project);
            case RESOLVED: return compilationService.build(project);
            default: return project;
        }
    }

    public Project cycleUpTo(Project project, Project.Status status) {
        while(project.getStatus().isLowerThan(status)) {
            LOGGER.info("Project {} is {}", project.getName(), project.getStatus());
            project = cycleUp(project);
            LOGGER.info("Project {} has been {}", project.getName(), project.getStatus());
        }
        return project;
    }
}
