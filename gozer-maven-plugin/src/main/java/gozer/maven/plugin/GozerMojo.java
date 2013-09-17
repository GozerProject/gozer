package gozer.maven.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Function;
import gozer.model.Project;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static gozer.model.builders.DependenciesBuilder.aDependencies;
import static gozer.model.builders.ProjectBuilder.aProject;

@Mojo(name = "gozer",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true)
@Execute(goal = "gozerize",
        phase = LifecyclePhase.PACKAGE)
public class GozerMojo {

    @Parameter( alias = "project.build.directory",
            required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;


    public void execute() throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File gozerProjectFile = new File(f, project.getName()+".gozer");

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

}
