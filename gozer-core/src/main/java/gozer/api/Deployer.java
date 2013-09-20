package gozer.api;

import gozer.model.Project;

public interface Deployer extends Lifecycle {

    Project deploy(Project project);
}
