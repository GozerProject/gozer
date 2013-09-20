package gozer.services;

import gozer.api.Cloner;
import gozer.api.Deployer;
import gozer.api.Lifecycle;
import gozer.api.Resolver;
import gozer.components.DefaultLifeCycle;
import gozer.components.LifeCycleEntry;
import gozer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

@Component
public class LifecycleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleService.class);
    private final DefaultLifeCycle defaultLifeCycle;

    public LifecycleService(DefaultLifeCycle defaultLifeCycle) {
        this.defaultLifeCycle = defaultLifeCycle;
    }

    public Project cycleUp(Project project) {

        LifeCycleEntry statusLifecycleEntry = defaultLifeCycle.getNextStatus(project.getStatus());
        Lifecycle service = statusLifecycleEntry.getService();

        switch (statusLifecycleEntry.getStatus()) {
            case CLONED: return ((Cloner)service).clone(project);
            case DEPLOYED: return ((Deployer)service).deploy(project);
            case RESOLVED: return ((Resolver)service).resolve(project);
            case COMPILED: return ((gozer.api.Compiler)service).build(project);
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
