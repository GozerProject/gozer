package gozer.admin.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GozerAdminWebServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GozerAdminWebServer.class);
    public static final String WEB_INF_LOCATION = "src/main/webapp/WEB-INF/web.xml";
    public static final String WEB_APP_LOCATION = "src/main/webapp";

    public static void main(String[] args) throws Exception {

        try {
            Server server = new Server(8080);
            WebAppContext handler = new WebAppContext();
            handler.setResourceBase(WEB_APP_LOCATION);
            handler.setContextPath("/admin");
            handler.setDescriptor(WEB_INF_LOCATION);
            server.setHandler(handler);
            server.start();
        } catch (Exception ex) {
            LOGGER.error("Error :",ex);
        }
    }
}
