package org.gozer.model;

import com.google.common.base.Objects;

public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging = "jar";

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
                .add("groupId", groupId)
                .add("artifactId", artifactId)
                .add("version", version)
                .toString();
    }
}
