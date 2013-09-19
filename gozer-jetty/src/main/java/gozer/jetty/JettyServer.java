package gozer.jetty;

import gozer.model.Project;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);
    private static final int DEFAULT_PORT = 8080;
    private Server server;

    public void execute(String contextPath) {
        String WEB_INF_LOCATION = "";
        String WEB_APP_LOCATION = "";

        server = new Server(DEFAULT_PORT);
        WebAppContext handler = new WebAppContext();
        handler.setResourceBase(WEB_APP_LOCATION);
        handler.setContextPath("/"+contextPath);
        handler.setDescriptor(WEB_INF_LOCATION);

        server.setHandler(handler);

    }

    public void addProject(Project project) {
        WebAppContext handler = new WebAppContext();
//        handler.setResourceBase(WEB_APP_LOCATION);
//        handler.setContextPath("/"+contextPath);
//        handler.setDescriptor(WEB_INF_LOCATION);
//        server.

    }

    public void start() {
        try {
            LOGGER.info("Starting Jetty Server");
            server.start();
            LOGGER.info("Jetty Server started");
        } catch (Exception e) {
            LOGGER.error("Failed to start Jetty Server", e);
        }
    }
}
