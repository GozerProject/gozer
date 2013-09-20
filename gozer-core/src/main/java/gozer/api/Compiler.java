package gozer.api;

import gozer.model.Project;

public interface Compiler extends Lifecycle {

    Project build(Project project);
}
