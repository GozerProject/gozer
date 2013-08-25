package org.gozer.model;

public class Project {

    private Long id;
    private String name;

    private Dependencies dependencies;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
