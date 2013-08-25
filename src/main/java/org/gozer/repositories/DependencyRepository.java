package org.gozer.repositories;

import com.google.common.annotations.VisibleForTesting;
import org.gozer.model.Dependency;
import restx.factory.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DependencyRepository {

    private HashSet<Dependency> dependencies;
    private Long index;

    public DependencyRepository() {
        index = 0L;
        dependencies = new HashSet<>();
    }

    public Set<Dependency> getAll() {
        return dependencies;
    }

    public Dependency create(Dependency dependency) {
        Dependency newDependency = new Dependency(dependency);
        newDependency.setId(++index);
        dependencies.add(newDependency);
        return newDependency;
    }

    @VisibleForTesting
    void setDependencies(HashSet<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    @VisibleForTesting
    HashSet<Dependency> getDependencies() {
        return dependencies;
    }
}
