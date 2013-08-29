package org.gozer.model;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

public class Project implements Serializable {
    private Set<File> dependenciesPaths;

//    private String classpath;

    public String getClasspath() {

        StringBuilder sb = new StringBuilder();
        for (File file : dependenciesPaths) {
//                    System.out.println("file : "+file.getAbsolutePath());
            sb.append(file.getAbsolutePath()).append(";");
        }

//        handler.setExtraClasspath(sb.toString()+"target/tmp/classes;target/git/spring-pet-clinic/src/main/resources/");

//
//        StringBuilder sb = new StringBuilder();
//        for (File file : directory.listFiles()) {
//            sb.append(file.getAbsolutePath()).append(";");
//        }
//
//        handler.setExtraClasspath(sb.toString()+"target/tmp/classes;target/git/spring-pet-clinic/src/main/resources/");


        return "";
    }

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
