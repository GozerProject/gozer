package gozer.services;

import gozer.model.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static gozer.model.builders.ProjectBuilder.aProject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LifecycleServiceTest {

    private LifecycleService lifecycleService;

    @Mock
    private GitService gitService;

    @Mock
    private CompilationService compilationService;

    @Mock
    private DependenciesService dependenciesService;
    private Project newProject;

    @Before
    public void initMocks() {
        lifecycleService = new LifecycleService(gitService, compilationService, dependenciesService);

        newProject = aProject().withName("test").withStatus(Project.Status.CREATED).build();
        Project projectCloned = aProject().withName("test").withStatus(Project.Status.CLONED).build();
        Project projectResolved = aProject().withName("test").withStatus(Project.Status.RESOLVED).build();
        Project projectCompiled = aProject().withName("test").withStatus(Project.Status.COMPILED).build();

        when(gitService.cloneRepository(newProject)).thenReturn(projectCloned);
        when(dependenciesService.resolve(projectCloned)).thenReturn(projectResolved);
        when(compilationService.build(projectResolved)).thenReturn(projectCompiled);
    }

    @Test
    public void cycleUpTo_should_update_CREATED_to_COMPILED() {
        Project returnedProject = lifecycleService.cycleUpTo(newProject, Project.Status.COMPILED);
        assertThat(returnedProject.getStatus()).isEqualTo(Project.Status.COMPILED);
    }

}
