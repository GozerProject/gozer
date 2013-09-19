package gozer.model.builders;

import gozer.model.Dependencies;
import gozer.model.Project;
import gozer.model.Status;

public class ProjectBuilder {
    private Long id;
    private String name;
    private Status status;
    private String scm;
    private Dependencies dependencies;
    private String path;

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

    public ProjectBuilder withStatus(Status status) {
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

    public ProjectBuilder withPath(String path) {
        this.path = path;
        return this;
    }

    public Project build() {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setStatus(status);
        project.setScm(scm);
        project.setDependencies(dependencies);
        project.setPath(path);
        return project;
    }
}
