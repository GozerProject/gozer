package org.gozer.services;

import org.gozer.model.Dependencies;
import org.gozer.model.Dependency;
import org.kevoree.resolver.MavenResolver;
import restx.factory.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Component
public class DependenciesService {

    private static final String MAVEN_CENTRAL = "http://repo1.maven.org/maven2";
    private static final String TARGET_REPOSITORY = "target/repository";
    private final MavenResolver resolver;

    public DependenciesService() {
        resolver = new MavenResolver();
        resolver.setBasePath(TARGET_REPOSITORY);
    }

    public File resolve(Dependency dependency) {
        return resolver.resolve(dependency.getMavenUrl(), Arrays.asList(MAVEN_CENTRAL));
    }

    public Set<File> resolve(Dependencies dependencies) {
        Set<File> files = newHashSet();

        for (Dependency dependency : dependencies.getCompile()) {
            files.add(resolve(dependency));
        }

        return files;
    }

}
