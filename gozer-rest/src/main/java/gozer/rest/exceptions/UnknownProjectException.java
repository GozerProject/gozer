package gozer.rest.exceptions;

public class UnknownProjectException extends RuntimeException {

    private final String projectName;

    public UnknownProjectException(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }
}
