package gozer.api;

import gozer.model.Project;

public interface Lifecycle {

    Project.Status getTo();
}
