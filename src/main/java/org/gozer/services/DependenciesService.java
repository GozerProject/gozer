package org.gozer.services;

import org.gozer.GozerFactory;
import org.gozer.model.Dependency;
import org.gozer.model.Project;
import org.kevoree.resolver.MavenResolver;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;
import java.util.Arrays;

@Component
public class DependenciesService {

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
        for (Dependency dependency : project.getDependencies().getCompile()) {
            project.addDependenciesPath(resolve(dependency));
        }

        project.setStatus(Project.Status.RESOLVED);

        return project;
    }

}
