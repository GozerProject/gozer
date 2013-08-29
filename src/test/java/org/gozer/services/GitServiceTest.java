package org.gozer.services;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.gozer.model.Project;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gozer.builders.ProjectBuilder.aProject;

public class GitServiceTest {


    @Test
    public void shouldCloneARepository() throws IOException, GitAPIException {
        Project project = aProject().withName("spring-pet-clinic")
                .withScm("https://github.com/SpringSource/spring-petclinic.git")
                .build();

        GitService gitService = new GitService();
        gitService.cloneRepository(project);

        assertThat(new File("target/git/spring-pet-clinic")).exists();
    }

    @After
    public void cleanClonedRepository() {
        File repoDir = new File("target/git/spring-pet-clinic");
        if (repoDir.exists()) {
            repoDir.delete();
        }
    }
}
