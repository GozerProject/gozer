package org.gozer.repositories;

import org.gozer.model.Project;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ProjectRepositoryTest {

    @Test
    public void getAll_should_return_a_empty_set() {
        ProjectRepository projectRepository = new ProjectRepository();
        Set<Project> projects = projectRepository.getAll();
        assertThat(projects).isNotNull().isEmpty();
    }

    @Test
    public void getAll_should_return_all_elements() {
        ProjectRepository projectRepository = new ProjectRepository();
        Project project1 = new Project();
        Project project2 = new Project();
        projectRepository.setProjects(newHashSet(project1, project2));
        Set<Project> projects = projectRepository.getAll();
        assertThat(projects).isNotNull().containsOnly(project1, project2);
    }
}
