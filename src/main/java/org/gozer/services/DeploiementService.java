package org.gozer.services;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.gozer.model.Project;
import restx.factory.Component;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.gozer.builders.ProjectBuilder.aProject;


@Component
public class DeploiementService {

    private static final String WEB_XML_STANDARD_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    private static final String WEBAPP_STANDARD_LOCATION = "src/main/webapp";
    private static final int DEFAULT_PORT = 8085;
    private String globalRepoPath = "target/git";

    public DeploiementService(String globalRepoPath) {
        this.globalRepoPath = globalRepoPath;
    }

    public void deploy(Project project) {
        String WEB_INF_LOCATION = globalRepoPath +"/"+ project.getName()+ "/" + WEB_XML_STANDARD_LOCATION;
        String WEB_APP_LOCATION = globalRepoPath +"/"+ project.getName()+ "/" + WEBAPP_STANDARD_LOCATION;

            try {
                Server server = new Server(DEFAULT_PORT);
                WebAppContext handler = new WebAppContext();
                handler.setResourceBase(WEB_APP_LOCATION);
                handler.setContextPath("/"+project.getName());


                // TODO create a method to build the classpath from project
                FileSystem fileSystem = FileSystems.getDefault();
                Path dependenciesRoot = fileSystem.getPath("");
                File directory = dependenciesRoot.toFile();
//                System.out.println("directory : "+directory.getAbsolutePath());
                StringBuilder sb = new StringBuilder();
                for (File file : directory.listFiles()) {
//                    System.out.println("file : "+file.getAbsolutePath());
                    sb.append(file.getAbsolutePath()).append(";");
                }

                handler.setExtraClasspath(sb.toString()+"target/tmp/classes;target/git/spring-pet-clinic/src/main/resources/");

                System.out.println("classpath : "+handler.getExtraClasspath());
                handler.setDescriptor(WEB_INF_LOCATION);
                server.setHandler(handler);
                server.start();


            } catch (Exception ex) {
                System.err.println(ex);
            }
    }

    public static void main(String[] args) throws Exception {

        Project project = aProject().withName("spring-pet-clinic")
                                    .withScm("https://github.com/SpringSource/spring-petclinic.git")
                                    .build();
        DeploiementService deploiementService = new DeploiementService("");
        deploiementService.deploy(project);
    }

}
