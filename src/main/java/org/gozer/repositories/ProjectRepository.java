package org.gozer.repositories;

import com.google.common.annotations.VisibleForTesting;
import org.gozer.model.Project;
import restx.factory.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ProjectRepository {

    private HashSet<Project> projects;
    private Long index;

    public ProjectRepository() {
        index = 0L;
        projects = new HashSet<>();
    }

    public Set<Project> getAll() {
        return projects;
    }

    public Project create(Project project) {
        Project newProject = new Project(project);
        newProject.setId(++index);
        projects.add(newProject);
        return newProject;
    }

    @VisibleForTesting
    void setProjects(HashSet<Project> projects) {
        this.projects = projects;
    }

    @VisibleForTesting
    HashSet<Project> getProjects() {
        return projects;
    }
}
