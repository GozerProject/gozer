package org.gozer.builders;

import org.gozer.model.Dependencies;
import org.gozer.model.Project;

public class ProjectBuilder {
    private Long id;
    private String name;
    private Project.Status status;
    private String scm;
    private Dependencies dependencies;

    private ProjectBuilder() {
    }

    public static ProjectBuilder aProject() {
        return new ProjectBuilder();
    }

    public ProjectBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ProjectBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProjectBuilder withStatus(Project.Status status) {
        this.status = status;
        return this;
    }

    public ProjectBuilder withScm(String scm) {
        this.scm = scm;
        return this;
    }

    public ProjectBuilder withDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public Project build() {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setStatus(status);
        project.setScm(scm);
        project.setDependencies(dependencies);
        return project;
    }
}
