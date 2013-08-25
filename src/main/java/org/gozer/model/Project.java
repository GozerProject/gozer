package org.gozer.model;

public class Project {

    private Long id;
    private String name;

    private Dependencies dependencies;

    public Project() {
    }

    public Project(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.dependencies = project.getDependencies();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }
}
