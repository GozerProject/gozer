package gozer.components;

import gozer.api.Lifecycle;
import gozer.model.Project;

public class LifeCycleEntry {
    Project.Status status;
    Lifecycle service;

    public LifeCycleEntry(Project.Status status, Lifecycle lifecycle) {
        this.status = status;
        this.service = lifecycle;
    }

    public Project.Status getStatus() {
        return status;
    }

    public Lifecycle getService() {
        return service;
    }
}