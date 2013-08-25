package org.gozer.repositories;

import org.gozer.model.Dependency;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class DependencyRepositoryTest {

    private DependencyRepository dependencyRepository;

    @Before
    public void setUp() throws Exception {
        dependencyRepository = new DependencyRepository();
    }

    @Test
    public void getAll_should_return_a_empty_set() {
        Set<Dependency> dependencies = dependencyRepository.getAll();
        assertThat(dependencies).isNotNull().isEmpty();
    }

    @Test
    public void getAll_should_return_all_elements() {
        Dependency dependency1 = new Dependency();
        Dependency dependency2 = new Dependency();
        dependencyRepository.setDependencies(newHashSet(dependency1, dependency2));
        Set<Dependency> dependencies = dependencyRepository.getAll();
        assertThat(dependencies).isNotNull().containsOnly(dependency1, dependency2);
    }

    @Test
    public void create_should_return_new_Dependency() {
        Dependency dependency = new Dependency();
        Dependency returnedDependency = dependencyRepository.create(dependency);
        assertThat(returnedDependency).isNotNull().isNotSameAs(dependency);
    }

    @Test
    public void create_should_store_new_Dependency() {
        Dependency dependency = new Dependency();
        Dependency returnedDependency = dependencyRepository.create(dependency);
        assertThat(returnedDependency).isNotNull();
        assertThat(dependencyRepository.getDependencies()).contains(returnedDependency);
    }

    @Test
    public void create_should_add_index_to_new_Dependency() {
        Dependency dependency = new Dependency();
        Dependency returnedDependency = dependencyRepository.create(dependency);
        assertThat(returnedDependency).isNotNull();
        assertThat(returnedDependency.getId()).isEqualTo(1L);
    }
}
