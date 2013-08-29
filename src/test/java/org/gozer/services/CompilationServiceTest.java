package org.gozer.services;

import org.assertj.core.util.Files;
import org.gozer.services.compiler.CompilationFinishedEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CompilationServiceTest {
    FileSystem fileSystem = FileSystems.getDefault();
    Path testSourceRoot = fileSystem.getPath("src/test/test-examples");
    Path sourceRoot = fileSystem.getPath("target/tmp/src");
    List<Path> sourceRoots = asList(sourceRoot);
    Path destination = fileSystem.getPath("target/tmp/classes");
    Collection<CompilationFinishedEvent> events = new ArrayList<>();
    Path dependenciesRoot = fileSystem.getPath("target/git/spring-pet-clinic/target/dependency");
    List<File> dependencies = Arrays.asList(dependenciesRoot.toFile().listFiles());

    @Before
    public void setup() {
        Files.delete(destination.toFile());
    }

    @Test
    public void should_build() throws Exception {
        CompilationService compilationManager = new CompilationService(sourceRoots, destination,dependencies);

        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isFalse();

        prepareSource("org/gozer/SimpleClass.java");
        compilationManager.build();
        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isTrue();

    }


    private void prepareSource(String path) throws IOException {
        File to = sourceRoot.resolve(path).toFile();
        to.getParentFile().mkdirs();
        com.google.common.io.Files.copy(
                testSourceRoot.resolve(path).toFile(),
                to);
    }
}