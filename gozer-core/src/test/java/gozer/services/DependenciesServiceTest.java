package gozer.services;

import gozer.model.Dependency;
import org.junit.Test;

import java.io.File;

import static gozer.model.builders.DependencyBuilder.aDependency;
import static org.assertj.core.api.Assertions.assertThat;

public class DependenciesServiceTest {

    @Test
    public void should_resolve_maven_dependency() {
        DependenciesService service = new DependenciesService("target/repositories");
        Dependency dependency = aDependency().withGroupId("org.apache.camel")
                                            .withArtifactId("camel-core")
                                            .withVersion("2.10.4")
                                            .build();
        service.resolve(dependency);

        assertThat(new File("target/repositories/org/apache/camel/camel-core/2.10.4/", "camel-core-2.10.4.jar")).exists();
    }
}
