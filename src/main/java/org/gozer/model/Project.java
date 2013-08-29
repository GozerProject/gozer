package org.gozer.model;

import java.io.Serializable;

public class Project implements Serializable {

    public enum Status {
        DEPLOYED;
    }


    private Long id;
    private String name;
    private Status status;
    private String scm;
    private Dependencies dependencies;

    public Project() {
        dependencies = new Dependencies();
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

    public String getScm() {
        return scm;
    }

    public void setScm(String scm) {
        this.scm = scm;
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
