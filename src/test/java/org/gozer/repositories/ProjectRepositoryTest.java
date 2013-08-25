package org.gozer.repositories;

import org.gozer.model.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ProjectRepositoryTest {

    private ProjectRepository projectRepository;

    @Before
    public void setUp() throws Exception {
        projectRepository = new ProjectRepository();
    }

    @Test
    public void getAll_should_return_a_empty_set() {
        ProjectRepository projectRepository = new ProjectRepository();
        Set<Project> projects = projectRepository.getAll();
        assertThat(projects).isNotNull().isEmpty();
    }

    @Test
    public void getAll_should_return_all_elements() {
        Project project1 = new Project();
        Project project2 = new Project();
        projectRepository.setProjects(newHashSet(project1, project2));
        Set<Project> projects = projectRepository.getAll();
        assertThat(projects).isNotNull().containsOnly(project1, project2);
    }

    @Test
    public void create_should_return_new_Project() {
        Project project = new Project();
        Project returnedProject = projectRepository.create(project);
        assertThat(returnedProject).isNotNull().isNotSameAs(project);
    }

    @Test
    public void create_should_store_new_Project() {
        Project project = new Project();
        Project returnedProject = projectRepository.create(project);
        assertThat(returnedProject).isNotNull();
        assertThat(projectRepository.getProjects()).contains(returnedProject);
    }

    @Test
    public void create_should_add_index_to_new_Project() {
        Project project = new Project();
        Project returnedProject = projectRepository.create(project);
        assertThat(returnedProject).isNotNull();
        assertThat(returnedProject.getId()).isEqualTo(1L);
    }
}
