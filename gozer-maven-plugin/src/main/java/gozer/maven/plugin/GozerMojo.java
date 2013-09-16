package gozer.maven.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Function;
import gozer.model.Project;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.RemoteRepositoryManager;

import java.io.File;
import java.io.IOException;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static gozer.model.builders.DependenciesBuilder.aDependencies;
import static gozer.model.builders.ProjectBuilder.aProject;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name = "foo",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true )
@Execute(goal = "compiler",
        lifecycle = "my-lifecycle",
        phase = LifecyclePhase.PACKAGE )

public class GozerMojo extends AbstractMojo {
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter( alias = "project.build.directory",
            required = true)
    private File outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;


    /**
     * @component
     */
    @Component
    protected ArtifactDescriptorReader artifactDescriptorReader;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="{repositorySystemSession}"
     * @readonly
     */
    @Parameter(defaultValue = "${repositorySystemSession}")
    protected MavenRepositorySystemSession repoSession;



    /**
            * @component
    */
    @Component
    protected ArtifactResolver artifactResolver;

    /**
     * @component
     */
    @Component
    protected RemoteRepositoryManager remoteRepositoryManager;

//    private Object invoke( Object object, String method )
//            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//        return object.getClass().getMethod( method ).invoke( object );
//    }
//
//    private ModelResolver makeModelResolver() throws MojoExecutionException {
//        try {
//            ProjectBuildingRequest projectBuildingRequest =
//                    (ProjectBuildingRequest) invoke( project, "getProjectBuildingRequest" );
//
//            Class c = Class.forName("org.apache.maven.repository.internal.DefaultModelResolver");
//            Constructor ct = c.getConstructor(new Class[]{RepositorySystemSession.class,
//                    RequestTrace.class, String.class,
//                    ArtifactResolver.class, RemoteRepositoryManager.class,
//                    List.class});
//            ct.setAccessible(true);
//            return (org.apache.maven.model.resolution.ModelResolver) ct.newInstance(new Object[]{
//                    projectBuildingRequest.getRepositorySession(),
//                    null, null, artifactResolver, remoteRepositoryManager,
//                    project.getRemoteProjectRepositories()});
//        } catch (Exception e) {
//            throw new MojoExecutionException("Error instantiating DefaultModelResolver", e);
//        }
//    }

//
//    public Model resolveEffectiveModel(File pomfile) {
//        try {
//            return modelBuilder.build(makeModelBuildRequest(pomfile)).getEffectiveModel();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private ModelBuildingRequest makeModelBuildRequest(File artifactFile) throws MojoExecutionException {
//        DefaultModelBuildingRequest mbr = new DefaultModelBuildingRequest();
//        mbr.setPomFile(artifactFile);
//        mbr.setModelResolver(makeModelResolver()); // <-- the hard-to-get modelResolver
//        return mbr;
//    }







    public void execute() throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File gozerProjectFile = new File(f, project.getName()+".gozer");
//        RepositorySystemSession repositorySystemSession = newSession();
//        Artifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(), project.getVersion());
//        ArtifactDescriptorRequest artifactDescriptorRequest = new ArtifactDescriptorRequest(artifact, Arrays.asList(new RemoteRepository("central", "maven2", "http://repo1.maven.org/maven2")), null );
//        ArtifactDescriptorResult result = null;
//        try {
//            result = artifactDescriptorReader.readArtifactDescriptor(repoSession, artifactDescriptorRequest);
//
//        } catch (ArtifactDescriptorException e) {
//            throw new MojoExecutionException("Error resolving effective POM ",e);
//        }

//        ModelResolver resolver = makeModelResolver();

//        ModelBuildingRequest modelBuildingRequest = makeModelBuildRequest(new File(""));



        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

            // TODO check that SCM field is filled
            Project gozerProject = aProject().withName(project.getArtifactId())
                                    .withScm(project.getScm().getUrl())
                                    .withDependencies(aDependencies().withCompileDependencies(newHashSet(transform(project.getDependencies(), new Function<Dependency, gozer.model.Dependency>() {
                                        @Override
                                        public gozer.model.Dependency apply(Dependency dependency) {
                                            gozer.model.Dependency gozerDependency = new gozer.model.Dependency();
                                            gozerDependency.setGroupId(dependency.getGroupId());
                                            gozerDependency.setArtifactId(dependency.getArtifactId());
                                            gozerDependency.setVersion(dependency.getVersion());
                                            gozerDependency.setPackaging(dependency.getType());
                                            return gozerDependency;
                                        }

                                    }))).build())
                                    .build();

            writer.writeValue(gozerProjectFile, gozerProject);

        } catch (IOException e ) {
            throw new MojoExecutionException( "Error creating file " + gozerProjectFile, e );
        }
    }

//    RepositorySystem newRepositorySystem() {
//        DefaultServiceLocator locator = new DefaultServiceLocator();
//        locator.setService(LocalRepositoryManagerFactory.class, EnhancedLocalRepositoryManagerFactory.class);
//        locator.setService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
//        locator.setService(RepositoryConnectorFactory.class, AsyncRepositoryConnectorFactory.class);
//        return locator.getService(RepositorySystem.class);
//    }

//    public MavenRepositorySystemSession newSession() {
//        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
//        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
//        session.setConfigProperty("aether.connector.ahc.provider", "jdk");
//        //TODO check this hack
//        LocalRepository localRepository = new LocalRepository(defaultLocalRepository());
//        session.setLocalRepositoryManager(newRepositorySystem().newLocalRepositoryManager(localRepository));
//        //TODO try to found maven configuration
//        File configFile = new File(defaultUserSettings());    // TODO is never used
//        session.getConfigProperties().put(ConfigurationProperties.REQUEST_TIMEOUT, 2000);
//        session.getConfigProperties().put(ConfigurationProperties.CONNECT_TIMEOUT, 1000);
//
//        return session;
//    }

//    String defaultUserSettings() {
//        return defaultLocalRepository() + "settings.xml";
//    }
//
//    String defaultLocalRepository() {
//        return System.getProperty("user.home") + File.separator +".m2"+ File.separator +"repository";
//    }
}
