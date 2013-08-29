package org.gozer.services;

import com.google.common.base.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.Hashing;
import org.gozer.services.compiler.CompilationFinishedEvent;
import org.gozer.services.compiler.FileWatchEvent;
import org.gozer.services.compiler.MoreFiles;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableCollection;

public class CompilationService {
    private static final Runnable NO_OP = new Runnable() {
        @Override
        public void run() {
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(CompilationService.class);
    private final EventBus eventBus;

    private final JavaCompiler javaCompiler;

    private final Iterable<Path> sourceRoots;
    private final Path destination;

    private final StandardJavaFileManager fileManager;

    // compile executor is a single thread, we can't perform compilations concurrently
    // all compilation will be done by the compile executor thread
    private final ScheduledExecutorService compileExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentLinkedDeque<Path> compileQueue = new ConcurrentLinkedDeque<>();

    private final Map<Path, SourceHash> hashes = new HashMap<>();

    private ExecutorService watcherExecutor;

    private volatile boolean compiling;

    // these parameters should be overridable, at least with system properties
    private final long compilationTimeout = 60; // in seconds
    private final int autoCompileQuietPeriod = 50; // ms
    private final boolean useLastModifiedTocheckChanges = true;
    private Collection<Diagnostic<?>> lastDiagnostics = new CopyOnWriteArrayList<>();

    public CompilationService(EventBus eventBus, Iterable<Path> sourceRoots, Path destination, List<File> dependencies) {
        this.eventBus = eventBus;
        this.sourceRoots = sourceRoots;
        this.destination = destination;

        javaCompiler = ToolProvider.getSystemJavaCompiler();
        fileManager = javaCompiler.getStandardFileManager(new DiagnosticCollector<JavaFileObject>(), Locale.ENGLISH, Charsets.UTF_8);
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

        loadHashes();

        eventBus.register(new Object() {
            @Subscribe
            public void onWatchEvent(FileWatchEvent event) {
                WatchEvent.Kind<Path> kind = event.getKind();
                Path source = event.getDir().resolve(event.getPath());
                if (!source.toFile().isFile()) {
                    return;
                }
                if (isSource(source)) {
                    if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                            || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (!queueCompile(source)) {
                            rebuild();
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        rebuild();
                    } else {
                        rebuild();
                    }
                } else {
                    copyResource(event.getDir(), event.getPath());
                }
            }
        });
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Path getDestination() {
        return destination;
    }

    public Iterable<Path> getSourceRoots() {
        return sourceRoots;
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

    private boolean queueCompile(final Path source) {
        boolean b = compileQueue.offerLast(source);
        if (!b) {
            return false;
        }
        compileExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                // nothing added since submission, quiet period is over
                if (compileQueue.getLast() == source) {
                    Collection<Path> sources = new HashSet<>();
                    while (!compileQueue.isEmpty()) {
                        sources.add(compileQueue.removeFirst());
                    }
                    compile(sources);
                }
            }
        }, autoCompileQuietPeriod, TimeUnit.MILLISECONDS);
        return true;
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

    public void startAutoCompile() {
        synchronized (this) {
            if (watcherExecutor == null) {
                watcherExecutor = Executors.newCachedThreadPool();
                for (Path sourceRoot : sourceRoots) {
                    MoreFiles.watch(sourceRoot, eventBus, watcherExecutor);
                }
            }
        }
    }

    public void stopAutoCompile() {
        synchronized (this) {
            if (watcherExecutor != null) {
                watcherExecutor.shutdownNow();
                watcherExecutor = null;
            }
        }
    }

    public void awaitAutoCompile() {
        try {
            if (compileQueue.isEmpty()) {
                // nothing in compile queue, we wait for current compilation if any by submitting a noop task
                // and waiting for it
                compileExecutor.submit(NO_OP).get(compilationTimeout, TimeUnit.SECONDS);
            } else {
                // we are in quiet period, let's submit a task after the quiet period and for it
                // if more file changes occur during that period we may miss them, but the purpose of this method
                // is to wait for autoCompile triggered *before* the call.
                compileExecutor.schedule(
                        NO_OP, autoCompileQuietPeriod + 10, TimeUnit.MILLISECONDS)
                        .get(compilationTimeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs an incremental compilation.
     */
    public void incrementalCompile() {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        final Collection<Path> sources = new ArrayList<>();
                        for (final Path sourceRoot : sourceRoots) {
                            if (sourceRoot.toFile().exists()) {
                                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        if (isSource(file)) {
                                            if (hasSourceChanged(sourceRoot, sourceRoot.relativize(file))) {
                                                sources.add(file);
                                            }
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
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSource(Path file) {
        return file.toString().endsWith(".java");
    }

    /**
     * Clean destination and do a full build.
     */
    public void rebuild() {
        try {
            Exception e = compileExecutor.submit(new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                    try {
                        compileQueue.clear();
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
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
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
                logger.debug("compilation finished: up to date");
                return;
            }

            // TODO ajout du classpath des dependences

            List<String> optionList = new ArrayList<>();
            FileSystem fileSystem = FileSystems.getDefault();
            Path dependenciesRoot = fileSystem.getPath("target/git/spring-pet-clinic/target/dependency");
            List<Path> dependenciesRoots = asList(dependenciesRoot);

            String classpath=System.getProperty("java.class.path");
            logger.info("classpath : {}", classpath);

//            optionList = Arrays.asList("-classpath", "/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-context-3.2.4.RELEASE.jar");
//            optionList.addAll(Arrays.asList("-classpath", classpath+":/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-core-4.2.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jdom-1.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/joda-time-jsptags-1.1.1.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/bootstrap-2.3.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hamcrest-core-1.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/FastInfoset-1.2.12.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-core-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/istack-commons-runtime-2.16.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/validation-api-1.0.0.GA.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/logback-classic-1.0.13.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-jdbc-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/itextpdf-5.3.4.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-webmvc-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/datatables-core-0.8.14.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/aopalliance-1.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-orm-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-tx-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-data-jpa-1.3.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/ehcache-core-2.6.6.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hsqldb-2.3.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/slf4j-api-1.7.5.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/commons-beanutils-1.8.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-context-support-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-aop-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-ehcache-4.2.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/dom4j-1.6.1.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/json-simple-1.1.1.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/usertype.spi-3.1.0.CR8.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jsp-api-2.2.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-expression-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-beans-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jstl-1.2.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jquery-1.9.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/logback-core-1.0.13.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/datatables-servlet2-0.8.14.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jcl-over-slf4j-1.7.1.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-jms-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-test-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-oxm-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/antlr-2.7.7.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/tomcat-juli-7.0.42.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/datatables-jsp-0.8.14.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/javassist-3.15.0-GA.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hamcrest-library-1.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-jpa-2.0-api-1.0.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jaxb-api-2.2.7.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jsr173_api-1.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-context-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-entitymanager-4.2.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/usertype.core-3.1.0.CR8.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/joda-time-hibernate-1.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-commons-annotations-4.0.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/junit-4.11.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/commons-lang-2.6.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/logback-access-1.0.9.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/servlet-api-2.5.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jboss-logging-3.1.0.GA.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/aspectjrt-1.7.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/datatables-export-itext-0.8.14.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jaxb-impl-2.2.7.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/rome-1.0.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jaxb-core-2.2.7.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-data-commons-1.5.2.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/tomcat-jdbc-7.0.42.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/spring-web-3.2.4.RELEASE.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/hibernate-validator-4.3.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jboss-transaction-api_1.1_spec-1.0.1.Final.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/joda-time-2.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/aspectjweaver-1.7.3.jar:/home/sebastien/workspace/gozer/target/git/spring-pet-clinic/target/dependency/jquery-ui-1.9.2.jar"));
//            optionList.addAll(Arrays.asList("-classpath", transform(dependenciesRoots, MoreFiles.pathToFileAsString).toString()));
//            logger.info("options : {}", optionList.get(1));

            JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
                    null, fileManager, diagnostics, null, null, javaFileObjects);

            boolean valid = compilationTask.call();
            if (valid) {
                for (Path source : sources) {
                    Path dir = null;
                    for (Path sourceRoot : sourceRoots) {
                        if ((source.isAbsolute() && source.startsWith(sourceRoot.toAbsolutePath()))
                                || (!source.isAbsolute() && source.startsWith(sourceRoot))) {
                            dir = sourceRoot;
                            break;
                        }
                    }
                    if (dir == null) {
                        logger.warn("can't find sourceRoot for {}", source);
                    } else {
                        SourceHash sourceHash = newSourceHashFor(dir, source.isAbsolute() ?
                                dir.toAbsolutePath().relativize(source) :
                                dir.relativize(source)
                        );
                        hashes.put(source.toAbsolutePath(), sourceHash);
                    }
                }

                saveHashes();

                logger.info("compilation finished: {} sources compiled in {}", sources.size(), stopwatch.stop());
                eventBus.post(new CompilationFinishedEvent(this, DateTime.now()));
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    logger.debug("{}", d);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    sb.append(d).append("\n");
                }
                lastDiagnostics.addAll(diagnostics.getDiagnostics());
                throw new RuntimeException("Compilation failed:\n" + sb);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            compiling = false;
        }
    }

    private void saveHashes() {
        File hashesFile = hashesFile();
        hashesFile.getParentFile().mkdirs();

        try (Writer w = com.google.common.io.Files.newWriter(hashesFile, Charsets.UTF_8)) {
            for (SourceHash sourceHash : hashes.values()) {
                w.write(sourceHash.serializeAsString());
                w.write("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHashes() {
        File hashesFile = hashesFile();
        if (hashesFile.exists()) {
            try (BufferedReader r = com.google.common.io.Files.newReader(hashesFile, Charsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    SourceHash sourceHash = parse(line);
                    hashes.put(sourceHash.getDir().resolve(sourceHash.getSourcePath()).toAbsolutePath(), sourceHash);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private File hashesFile() {
        return destination.resolve("META-INF/.hashes").toFile();
    }

    /**
     * @return true if this compilation manager is currently performing a compilation task.
     */
    public boolean isCompiling() {
        return compiling;
    }

    private boolean hasSourceChanged(Path dir, Path source) {
        try {
            SourceHash sourceHash = hashes.get(dir.resolve(source).toAbsolutePath());
            if (sourceHash != null) {
                return sourceHash.hasChanged() != sourceHash;
            } else {
                return true;
            }
        } catch (IOException e) {
            return true;
        }
    }

    private class SourceHash {
        private final Path dir;
        private final Path sourcePath;
        private final String hash;
        private final long lastModified;

        private SourceHash(Path dir, Path sourcePath, String hash, long lastModified) {
            this.dir = dir;
            this.sourcePath = sourcePath;
            this.hash = hash;
            this.lastModified = lastModified;
        }

        @Override
        public String toString() {
            return "SourceHash{" +
                    "dir=" + dir +
                    ", sourcePath=" + sourcePath +
                    ", hash='" + hash + '\'' +
                    ", lastModified=" + lastModified +
                    '}';
        }

        public Path getDir() {
            return dir;
        }

        public Path getSourcePath() {
            return sourcePath;
        }

        public String getHash() {
            return hash;
        }

        public long getLastModified() {
            return lastModified;
        }

        public SourceHash hasChanged() throws IOException {
            File sourceFile = dir.resolve(sourcePath).toFile();
            if (useLastModifiedTocheckChanges) {
                if (lastModified < sourceFile.lastModified()) {
                    return new SourceHash(dir, sourcePath,
                            computeHash(), sourceFile.lastModified());
                }
            } else {
                String currentHash = computeHash();
                if (!currentHash.equals(hash)) {
                    return new SourceHash(dir, sourcePath,
                            currentHash, sourceFile.lastModified());
                }
            }
            return this;
        }

        private String computeHash() throws IOException {
            return hash(dir.resolve(sourcePath).toFile());
        }

        public String serializeAsString() throws IOException {
            return Joiner.on("**").join(dir, sourcePath, hash, lastModified);
        }
    }

    private SourceHash newSourceHashFor(Path dir, Path sourcePath) throws IOException {
        File sourceFile = dir.resolve(sourcePath).toFile();
        return new SourceHash(dir, sourcePath, hash(sourceFile), sourceFile.lastModified());
    }

    private String hash(File file) throws IOException {
        return com.google.common.io.Files.hash(file, Hashing.md5()).toString();
    }

    private SourceHash parse(String str) {
        Iterator<String> parts = Splitter.on("**").split(str).iterator();
        FileSystem fileSystem = FileSystems.getDefault();
        return new SourceHash(
                fileSystem.getPath(parts.next()),
                fileSystem.getPath(parts.next()),
                parts.next(),
                Long.parseLong(parts.next())
        );
    }

    public static void main(String[] args) {

        FileSystem fileSystem = FileSystems.getDefault();
        Path sourceRoot = fileSystem.getPath("target/git/spring-pet-clinic/src/main/java");
        Path destination = fileSystem.getPath("target/tmp/classes");
        Path dependenciesRoot = fileSystem.getPath("target/git/spring-pet-clinic/target/dependency");
        List<File> dependencies = Arrays.asList(dependenciesRoot.toFile().listFiles());
        List<Path> sourceRoots = asList(sourceRoot);
        EventBus eventBus = new EventBus();
        CompilationService compilationService = new CompilationService(eventBus, sourceRoots, destination, dependencies);
        compilationService.rebuild();
    }

}