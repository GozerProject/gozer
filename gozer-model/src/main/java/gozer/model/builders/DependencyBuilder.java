package gozer.model.builders;

import gozer.model.Dependency;

public class DependencyBuilder {
    private String groupId;
    private String artifactId;
    private String version;

    private DependencyBuilder() {
    }

    public static DependencyBuilder aDependency() {
        return new DependencyBuilder();
    }

    public DependencyBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public DependencyBuilder withArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public DependencyBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public Dependency build() {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }
}
