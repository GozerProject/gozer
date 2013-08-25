package org.gozer.model;

import java.util.HashSet;
import java.util.Set;

public class Dependencies {

    private Set<Dependency> compile;

    public Dependencies() {
        compile = new HashSet<Dependency>();
    }

    public Set<Dependency> getCompile() {
        return compile;
    }

    public void setCompile(Set<Dependency> compile) {
        this.compile = compile;
    }
}
