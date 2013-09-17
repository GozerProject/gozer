package gozer.model.builders;

import gozer.model.Dependencies;
import gozer.model.Dependency;

import java.util.Set;

public class DependenciesBuilder {
    private Set<Dependency> compileDependencies;

    private DependenciesBuilder() {
    }

    public static DependenciesBuilder aDependencies() {
        return new DependenciesBuilder();
    }

    public DependenciesBuilder withCompileDependencies(Set<Dependency> compileDependencies) {
        this.compileDependencies = compileDependencies;
        return this;
    }

    public Dependencies build() {
        Dependencies dependencies = new Dependencies();
        dependencies.setCompile(compileDependencies);
        return dependencies;
    }
}
