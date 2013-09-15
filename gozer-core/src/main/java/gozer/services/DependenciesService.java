package gozer.services;

import gozer.GozerFactory;
import gozer.model.Dependency;
import gozer.model.Project;
import org.kevoree.resolver.MavenResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;
import java.util.Arrays;

@Component
public class DependenciesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependenciesService.class);
    private static final String MAVEN_CENTRAL = "http://repo1.maven.org/maven2";
    private final MavenResolver resolver;

    public DependenciesService(@Named(GozerFactory.DEPENDENCIES_REPOSITORY) String dependenciesRepository) {
        resolver = new MavenResolver();
        resolver.setBasePath(dependenciesRepository);
    }

    public File resolve(Dependency dependency) {
        return resolver.resolve(dependency.getMavenUrl(), Arrays.asList(MAVEN_CENTRAL));
    }

    public Project resolve(Project project) {
        if (project.hasBeenResolved()) {
            project.cleanDependenciesPath();
        }

        LOGGER.debug("resolving project {}", project);
        for (Dependency dependency : project.getDependencies().getCompile()) {
            LOGGER.debug("add {} as a dependency to project {}", dependency, project.getName());
            File dependencyFile = resolve(dependency);
            LOGGER.debug("path of the dependency : {}", dependencyFile.getAbsolutePath());
            project.addDependenciesPath(dependencyFile);
        }

        project.setStatus(Project.Status.RESOLVED);
        LOGGER.info("Project {} has been {}", project.getName(), project.getStatus());
        return project;
    }

}
