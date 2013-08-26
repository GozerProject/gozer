package org.gozer.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Dependencies implements Serializable {

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
