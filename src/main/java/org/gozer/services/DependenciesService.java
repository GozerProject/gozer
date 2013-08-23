package org.gozer.services;

import org.gozer.model.Dependency;
import org.kevoree.resolver.MavenResolver;

import java.io.File;
import java.util.Arrays;

public class DependenciesService {

    private static final String MAVEN_CENTRAL = "http://repo1.maven.org/maven2";
    private static final String TARGET_REPOSITORY = "target/repository";

    public void resolve(Dependency dependency) {
        MavenResolver resolver = new MavenResolver();
        resolver.setBasePath(TARGET_REPOSITORY);
        File cachedFile = resolver.resolve(dependency.getMavenUrl(), Arrays.asList(MAVEN_CENTRAL));
    }

}
