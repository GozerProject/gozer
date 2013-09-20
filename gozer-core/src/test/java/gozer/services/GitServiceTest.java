package gozer.services;

import gozer.model.Project;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static gozer.model.Project.Status.CLONED;
import static gozer.model.builders.ProjectBuilder.aProject;
import static org.assertj.core.api.Assertions.assertThat;

public class GitServiceTest {

    @Test
    public void shouldCloneARepository() throws IOException, GitAPIException {
        Project project = aProject().withName("spring-pet-clinic")
                .withScm("https://github.com/SpringSource/spring-petclinic.git")
                .build();

        GitService gitService = new GitService("target/git");
        Project returnedProject = gitService.clone(project);

        assertThat(new File("target/git/spring-pet-clinic")).exists();
        assertThat(returnedProject).isNotNull();
        assertThat(returnedProject.getStatus()).isEqualTo(CLONED);
        assertThat(returnedProject.getPath()).isEqualTo("target/git/spring-pet-clinic");
    }

    @After
    public void cleanClonedRepository() {
        File repoDir = new File("target/git/spring-pet-clinic");
        if (repoDir.exists()) {
            repoDir.delete();
        }
    }
}
