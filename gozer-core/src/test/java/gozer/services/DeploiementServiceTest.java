package gozer.services;

import gozer.model.Project;
import org.junit.Ignore;
import org.junit.Test;

import static gozer.model.builders.ProjectBuilder.aProject;

public class DeploiementServiceTest {

    @Test
    @Ignore
    public void should_deploy_project() {
        Project project = aProject().withName("spring-pet-clinic")
                                    .build();
        // TODO inject DependenciesService
        DeploiementService deploiementService = new DeploiementService("target/git", "target/temp/classes", null, null);
        deploiementService.deploy(project);
    }
}
