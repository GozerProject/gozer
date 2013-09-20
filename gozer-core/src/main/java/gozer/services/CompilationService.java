package gozer.services;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import gozer.GozerFactory;
import gozer.components.DefaultLifeCycle;
import gozer.model.Project;
import gozer.services.compiler.MoreFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

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

@Component
public class CompilationService implements gozer.api.Compiler {

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

    public CompilationService(@Named(GozerFactory.COMPILATION_DESTINATION) String compilationDestination,
                              DefaultLifeCycle defaultLifecycle) {
        defaultLifecycle.register(this);
        FileSystem fileSystem = FileSystems.getDefault();
        destination = fileSystem.getPath(compilationDestination);

        javaCompiler = ToolProvider.getSystemJavaCompiler();
        fileManager = javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), Locale.ENGLISH, Charsets.UTF_8);

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
    @Override
    public Project build(Project project) {
        LOGGER.debug("Building project {}", project);
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
                LOGGER.info("Compilation finished: {} sources compiled in {}", sources.size(), stopwatch.stop());
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

    @Override
    public Project.Status getTo() {
        return Project.Status.COMPILED;
    }
}