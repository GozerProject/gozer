package org.gozer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Project implements Serializable {
    public enum Status {
        DEPLOYED, CLONED, COMPILED, RESOLVED;

    }
    private Long id;

    private String name;
    private Status status;
    private String scm;
    private Dependencies dependencies;
    private Set<File> dependenciesPaths;
    private String path;

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

    public String getPath() {
        return path;
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

    public Set<File> getDependenciesPaths() {
        return dependenciesPaths;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @JsonIgnore
    public List<Path> getSourcePaths() {
        FileSystem fileSystem = FileSystems.getDefault();
        Path sourceRoot = fileSystem.getPath(path+"/src/main/java");

        return Arrays.asList(sourceRoot);
    }

    public void addDependenciesPath(File dependencyPath) {
        dependenciesPaths.add(dependencyPath);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("status", status)
                .add("scm", scm)
                .add("dependencies", dependencies)
                .add("dependenciesPaths", dependenciesPaths)
                .add("path", path)
                .toString();
    }
}
