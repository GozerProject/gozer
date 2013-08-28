package org.gozer.services;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class DeploiementService {


    public void deploy() throws Exception {
        String WEB_INF_LOCATION = "target/git/src/main/webapp/WEB-INF/web.xml";
        String WEB_APP_LOCATION = "target/git/src/main/webapp";


//        WebServer server = new JettyWebServer(WEB_INF_LOCATION, WEB_APP_LOCATION, 8085, "0.0.0.0");
//        server.startAndAwait();




            try {
                Server server = new Server(8085);
                WebAppContext handler = new WebAppContext();
                handler.setResourceBase(WEB_APP_LOCATION);
                handler.setContextPath("/appli");
                handler.setExtraClasspath("target/tmp/classes");
                handler.setDescriptor(WEB_INF_LOCATION);
                server.setHandler(handler);
                server.start();


            } catch (Exception ex) {
                System.err.println(ex);
            }
    }

//    public static void main(String[] args) throws Exception {
//        Server server = new Server();
//        SocketConnector connector = new SocketConnector();
//
//        // Set some timeout options to make debugging easier.
//        connector.setMaxIdleTime(1000 * 60 * 60);
//        connector.setSoLingerTime(-1);
//        connector.setPort(8080);
//        server.setConnectors(new Connector[] { connector });
//
//        WebAppContext context = new WebAppContext();
//        context.setServer(server);
//        context.setContextPath("/");
//
//        server.addHandler(context);
//        try {
//            server.start();
//            System.in.read();
//            server.stop();
//            server.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(100);
//        }
//    }


    public static void main(String[] args) throws Exception {
        DeploiementService deploiementService = new DeploiementService();
        deploiementService.deploy();
    }

}
