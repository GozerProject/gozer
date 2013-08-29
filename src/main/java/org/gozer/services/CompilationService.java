package org.gozer.services;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import org.gozer.GozerFactory;
import org.gozer.model.Project;
import org.gozer.services.compiler.MoreFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableCollection;
import static org.gozer.builders.ProjectBuilder.aProject;

public class CompilationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompilationService.class);

    private final JavaCompiler javaCompiler;

//    private final Iterable<Path> sourceRoots;
    private final Path destination;

    private final StandardJavaFileManager fileManager;

    // compile executor is a single thread, we can't perform compilations concurrently
    // all compilation will be done by the compile executor thread
    private final ScheduledExecutorService compileExecutor = Executors.newSingleThreadScheduledExecutor();

    private volatile boolean compiling;

    // these parameters should be overridable, at least with system properties
    private final long compilationTimeout = 60; // in seconds
    private Collection<Diagnostic<?>> lastDiagnostics = new CopyOnWriteArrayList<>();

    public CompilationService(@Named(GozerFactory.COMPILATION_DESTINATION) String compilationDestination) {
        FileSystem fileSystem = FileSystems.getDefault();
        destination = fileSystem.getPath(compilationDestination);

        javaCompiler = ToolProvider.getSystemJavaCompiler();
        fileManager = javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), Locale.ENGLISH, Charsets.UTF_8);

    }

    public Path getDestination() {
        return destination;
    }

    private void copyResource(final Path dir, final Path resourcePath) {
        compileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                File source = dir.resolve(resourcePath).toFile();
                if (source.isFile()) {
                    try {
                        File to = destination.resolve(resourcePath).toFile();
                        to.getParentFile().mkdirs();
                        if (!to.exists() || to.lastModified() < source.lastModified()) {
                            com.google.common.io.Files.copy(source, to);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    /**
     * Returns the path of the .class file containing bytecode for the given class (by name).
     *
     * @param className the class for which the class file should be returned
     * @return the Path of the class file, absent if it doesn't exists.
     */
    public Optional<Path> getClassFile(String className) {
        Path classFilePath = destination.resolve(className.replace('.', '/') + ".class");
        if (classFilePath.toFile().exists()) {
            return Optional.of(classFilePath);
        } else {
            return Optional.absent();
        }
    }

    private boolean isSource(Path file) {
        return file.toString().endsWith(".java");
    }

    /**
     * Clean destination and do a full build.
     */
    public Project build(Project project) {

        Set<File> dependencies = project.getDependenciesPaths();
        final List<Path> sourceRoots = project.getSourcePaths();

         try {
            if (!destination.toFile().exists()) {
                destination.toFile().mkdirs();
            }

            fileManager.setLocation(StandardLocation.SOURCE_PATH, transform(sourceRoots, MoreFiles.pathToFile));
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, singleton(destination.toFile()));
            fileManager.setLocation(StandardLocation.CLASS_PATH, dependencies);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        MoreFiles.delete(destination);
                        destination.toFile().mkdirs();

                        final Collection<Path> sources = new ArrayList<>();
                        for (final Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (isSource(file)) {
                                            sources.add(file);
                                        } else if (file.toFile().isFile()) {
                                            copyResource(sourceRoot, sourceRoot.relativize(file));
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            }
                        }

                        compile(sources);
                        return null;
                    } catch (Exception e) {
                        return e;
                    }
                }
            }).get(compilationTimeout, TimeUnit.SECONDS);
            if (e != null) {
                throw new RuntimeException(e);
            } else {
                project.setStatus(Project.Status.COMPILED);
                return project;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void compileSources(final Path... sources) {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    compile(asList(sources));
                    return null;
                }
            }).get(compilationTimeout, TimeUnit.SECONDS);
            if (e != null) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Diagnostic<?>> getLastDiagnostics() {
        return unmodifiableCollection(lastDiagnostics);
    }

    private void compile(Collection<Path> sources) {
        // MUST BE CALLED in compileExecutor only
        Stopwatch stopwatch = new Stopwatch().start();
        compiling = true;
        try {
            lastDiagnostics.clear();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            Iterable<? extends JavaFileObject> javaFileObjects =
                    fileManager.getJavaFileObjectsFromFiles(transform(sources, MoreFiles.pathToFile));

            if (isEmpty(javaFileObjects)) {
                LOGGER.debug("compilation finished: up to date");
                return;
            }

            JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
                    null, fileManager, diagnostics, null, null, javaFileObjects);

            boolean valid = compilationTask.call();
            if (valid) {
//                for (Path source : sources) {
//                    Path dir = null;
//                    for (Path sourceRoot : sourceRoots) {
//                        if ((source.isAbsolute() && source.startsWith(sourceRoot.toAbsolutePath()))
//                                || (!source.isAbsolute() && source.startsWith(sourceRoot))) {
//                            dir = sourceRoot;
//                            break;
//                        }
//                    }
//                    if (dir == null) {
//                        LOGGER.warn("can't find sourceRoot for {}", source);
//                    } else {
//                        SourceHash sourceHash = newSourceHashFor(dir, source.isAbsolute() ?
//                                dir.toAbsolutePath().relativize(source) :
//                                dir.relativize(source)
//                        );
//                    }
//                }

//                saveHashes();

                LOGGER.info("compilation finished: {} sources compiled in {}", sources.size(), stopwatch.stop());
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    LOGGER.debug("{}", d);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    sb.append(d).append("\n");
                }
                lastDiagnostics.addAll(diagnostics.getDiagnostics());
                throw new RuntimeException("Compilation failed:\n" + sb);
            }
        } finally {
            compiling = false;
        }
    }

    /**
     * @return true if this compilation manager is currently performing a compilation task.
     */
    public boolean isCompiling() {
        return compiling;
    }

    public static void main(String[] args) {

        Project project = aProject().withName("spring-pet-clinic")
                .withScm("https://github.com/SpringSource/spring-petclinic.git")
                .build();

        FileSystem fileSystem = FileSystems.getDefault();
        Path sourceRoot = fileSystem.getPath("target/git/spring-pet-clinic/src/main/java");
        Path dependenciesRoot = fileSystem.getPath("target/git/spring-pet-clinic/target/dependency");
//        for (File file : dependenciesRoot.toFile().listFiles()) {
//            System.out.println(file.getName());
//        }

        List<File> dependencies = Arrays.asList(dependenciesRoot.toFile().listFiles());
        List<Path> sourceRoots = asList(sourceRoot);
        CompilationService compilationService = new CompilationService("target/tmp/classes");
        compilationService.build(project);
    }

}