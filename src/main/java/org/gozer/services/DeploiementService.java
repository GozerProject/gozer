package org.gozer.services;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.gozer.GozerFactory;
import org.gozer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;


@Component
public class DeploiementService {


    // TODO put everything into injected configuration
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploiementService.class);
    private static final String WEB_XML_STANDARD_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    private static final String WEBAPP_STANDARD_LOCATION = "src/main/webapp";
    private static final int DEFAULT_PORT = 8085;
    private String classesPath;
    private String globalRepoPath;

    public DeploiementService(@Named(GozerFactory.GLOBAL_REPO_PATH) String globalRepoPath,
                              @Named(GozerFactory.COMPILATION_DESTINATION) String classesPath) {
        this.globalRepoPath = globalRepoPath;
        this.classesPath = classesPath;
    }

    public Project deploy(Project project) {
        String WEB_INF_LOCATION = globalRepoPath +"/"+ project.getName()+ "/" + WEB_XML_STANDARD_LOCATION;
        String WEB_APP_LOCATION = globalRepoPath +"/"+ project.getName()+ "/" + WEBAPP_STANDARD_LOCATION;

            try {
                Server server = new Server(DEFAULT_PORT);
                WebAppContext handler = new WebAppContext();
                handler.setResourceBase(WEB_APP_LOCATION);
                handler.setContextPath("/"+project.getName());
                handler.setExtraClasspath(buildDeploymentClasspath(project));
                LOGGER.debug("classpath : {}", handler.getExtraClasspath());
                handler.setDescriptor(WEB_INF_LOCATION);
                server.setHandler(handler);
                server.start();
            } catch (Exception ex) {
                LOGGER.error("Error :",ex);
            }
        project.setStatus(Project.Status.DEPLOYED);
        return project;
    }

    public String buildDeploymentClasspath(Project project) {
        StringBuilder sb = new StringBuilder();
        // add all dependencies to classpath
        for (File file : project.getDependenciesPaths()) {
            sb.append(file.getAbsolutePath()).append(";");
        }
        // add compiled classes to classpath
        sb.append(classesPath).append(";");
        // add resources to classpath TODO merge into compiled directory
        sb.append(globalRepoPath+"/"+project.getName()+"/src/main/resources/");
        LOGGER.debug("classpath : {}", sb);
        return sb.toString();
    }

}
