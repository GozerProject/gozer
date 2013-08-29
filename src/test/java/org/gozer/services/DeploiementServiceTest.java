package org.gozer.services;

import org.gozer.model.Project;
import org.junit.Test;

import static org.gozer.builders.ProjectBuilder.aProject;

public class DeploiementServiceTest {

    @Test
    public void should_deploy_project() {
        Project project = aProject().withName("spring-pet-clinic")
                                    .build();
        DeploiementService deploiementService = new DeploiementService("target/git", "target/temp/classes");
        deploiementService.deploy(project);
    }
}
