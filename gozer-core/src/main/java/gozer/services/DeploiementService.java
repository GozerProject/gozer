package gozer.services;

import gozer.GozerFactory;
import gozer.model.Project;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kevoree.kcl.KevoreeJarClassLoader;
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
            LOGGER.debug("classpath : {}", handler.getExtraClasspath());
            handler.setDescriptor(WEB_INF_LOCATION);



            KevoreeJarClassLoader kclScope1 = new KevoreeJarClassLoader();
            kclScope1.isolateFromSystem();

            for (File dependencyPath : project.getDependenciesPaths()) {
                KevoreeJarClassLoader kclScope = new KevoreeJarClassLoader();
                kclScope.addJarFromURL(dependencyPath.toURI().toURL());
                LOGGER.info("deploiement jar path : {}", dependencyPath.toURI().toURL());
                kclScope.addChild(kclScope1);
            }

            handler.setClassLoader(kclScope1);
            server.setHandler(handler);
            server.start();

        } catch (Exception ex) {
            LOGGER.error("Error :",ex);
        }
        project.setStatus(Project.Status.DEPLOYED);
        return project;
    }

}
