package org.gozer.services;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
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
    EventBus eventBus = new EventBus();
    Collection<CompilationFinishedEvent> events = new ArrayList<>();
    Path dependenciesRoot = fileSystem.getPath("target/git/spring-pet-clinic/target/dependency");
    List<File> dependencies = Arrays.asList(dependenciesRoot.toFile().listFiles());

    @Before
    public void setup() {
        Files.delete(destination.toFile());
        eventBus.register(new Object() {
            @Subscribe
            public void onCompilationFinished(CompilationFinishedEvent event) {
                events.add(event);
            }
        });
    }

//    @Test
//    public void test() throws IOException {
//        File[] files = new File[] {new File("org/gozer/SimpleCal")};
//        String classpath=System.getProperty("java.class.path");
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        List<String> optionList = new ArrayList<String>();
//        optionList.addAll(Arrays.asList("-classpath", "/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-context-3.2.4.RELEASE.jar"));
////      optionList.addAll(Arrays.asList("-d",rootPath+"/target"));
//        StandardJavaFileManager sjfm = compiler.getStandardFileManager(null, null, null);
//        Iterable fileObjects = sjfm.getJavaFileObjects(files);
//        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null,optionList,null,fileObjects);
//        task.call();
//        sjfm.close();
//    }

    @Test
    public void should_rebuild_build() throws Exception {
        CompilationService compilationManager = new CompilationService(eventBus, sourceRoots, destination,dependencies);

        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isFalse();

        prepareSource("org/gozer/SimpleClass.java");
        compilationManager.rebuild();
        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);

        // rebuild should trigger a new compilation
        compilationManager.rebuild();
        assertThat(events).hasSize(2);
    }

    @Test
    public void should_incremental_build() throws Exception {
        CompilationService compilationManager = new CompilationService(eventBus, sourceRoots, destination,dependencies);

        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isFalse();

        prepareSource("org/gozer/SimpleClass.java");
        compilationManager.incrementalCompile();
        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);

        // incremental compile should do nothing
        compilationManager.incrementalCompile();
        assertThat(compilationManager.getClassFile("org.gozer.SimpleClass").isPresent()).isTrue();
        assertThat(events).hasSize(1);
    }


    private void prepareSource(String path) throws IOException {
        File to = sourceRoot.resolve(path).toFile();
        to.getParentFile().mkdirs();
        com.google.common.io.Files.copy(
                testSourceRoot.resolve(path).toFile(),
                to);
    }
}