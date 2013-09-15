package gozer.repositories;

import com.google.common.collect.ImmutableMap;
import gozer.model.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static gozer.model.builders.ProjectBuilder.aProject;
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
        Collection<Project> projects = projectRepository.getAll();
        assertThat(projects).isNotNull().isEmpty();
    }

    @Test
    public void getAll_should_return_all_elements() {
        Project project1 = new Project();
        Project project2 = new Project();
        projectRepository.setProjects(ImmutableMap.of(1L, project1, 2L, project2));
        Collection<Project> projects = projectRepository.getAll();
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
        assertThat(projectRepository.getProjects()).containsValue(returnedProject);
    }

    @Test
    public void create_should_add_index_to_new_Project() {
        Project project = new Project();
        Project returnedProject = projectRepository.create(project);
        assertThat(returnedProject).isNotNull();
        assertThat(returnedProject.getId()).isEqualTo(1L);
    }

    @Test
    public void findByName_should_get_a_Project_with_the_name() {
        projectRepository.setProjects(ImmutableMap.of(1L, aProject().withName("name").build()));
        Project returnedProject = projectRepository.findByName("name");
        assertThat(returnedProject).isNotNull();
        assertThat(returnedProject.getName()).isEqualTo("name");
    }

    @Test(expected = NullPointerException.class)
    public void findByName_should_have_null_parameter() {
        projectRepository.findByName(null);
    }
}
