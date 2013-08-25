package org.gozer.repositories;

import com.google.common.annotations.VisibleForTesting;
import org.gozer.model.Project;
import restx.factory.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ProjectRepository {

    private HashSet<Project> projects;

    public ProjectRepository() {
        projects = new HashSet<>();
    }


    public Set<Project> getAll() {
        return projects;
    }

    @VisibleForTesting
    void setProjects(HashSet<Project> projects) {
        this.projects = projects;
    }

}
