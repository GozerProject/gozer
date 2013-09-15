package gozer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

import java.io.Serializable;

public class Dependency implements Serializable {

    private Long id;
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging = "jar";

    public Dependency() {
    }

    public Dependency(Dependency dependency) {
        this.id = dependency.getId();
        this.groupId = dependency.getGroupId();
        this.artifactId = dependency.getArtifactId();
        this.version = dependency.getVersion();
        this.packaging = dependency.getPackaging();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    @JsonIgnore
    public String getMavenUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("mvn").append(":")
          .append(groupId).append(":")
          .append(artifactId).append(":")
          .append(version).append(":")
          .append(packaging);
        return sb.toString();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("", groupId)
                .add("", artifactId)
                .add("", version)
                .add("", packaging)
                .toString();
    }
}
