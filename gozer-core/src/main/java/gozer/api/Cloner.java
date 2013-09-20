package gozer.api;

import gozer.model.Project;

public interface Cloner extends Lifecycle {

    Project clone(Project project);
}
