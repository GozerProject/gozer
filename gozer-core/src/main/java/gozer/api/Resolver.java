package gozer.api;

import gozer.model.Project;

public interface Resolver extends Lifecycle {

    Project resolve(Project project);
}
