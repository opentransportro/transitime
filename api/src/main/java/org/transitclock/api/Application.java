package org.transitclock.api;

import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.ee10.jsp.JettyJspServlet;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ErrorHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.servlet.ServletContainer;
import org.transitclock.api.utils.ApiLoggingFilter;
import org.transitclock.api.utils.WebLoggingFilter;
import org.transitclock.api.utils.XSSFilter;
import org.transitclock.web.ReadConfigListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;

@Slf4j
public class Application {

    // Resource path pointing to where the WEBROOT is
    private static final String WEBROOT_INDEX = "/webroot/";
    private final Server server;

    public static void main(String[] args) throws IOException, URISyntaxException {
        Application application = new Application();
        application.start();
    }

    public Application() throws IOException, URISyntaxException {
        server = new Server();

        // Define ServerConnector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        // Base URI for servlet context
        URI baseUri = getWebRootResourceUri();

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setInitParameter("transitime_config_file_location", System.getProperty("transitclock.configFiles"));
        servletContextHandler.setErrorHandler(new ErrorHandler());
        servletContextHandler.addEventListener(new ReadConfigListener());
        servletContextHandler.setContextPath("/");
        servletContextHandler.addFilter(new XSSFilter(), "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(new ApiLoggingFilter(), "/api/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(new WebLoggingFilter(), "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.setBaseResourceAsString(baseUri.toASCIIString());

        enableEmbeddedJspSupport(servletContextHandler);

        // Default Servlet (always last, always named "default")
        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed", "true");
        holderDefault.setInitOrder(0);
        servletContextHandler.addServlet(holderDefault, "/");

        ServletHolder docServlet = new ServletHolder("doc", ServletContainer.class);
        docServlet.setInitParameter("jersey.config.server.provider.packages", "io.swagger.v3.jaxrs2.integration.resources,org.transitclock.api.resources,org.transitclock.api.utils");
        docServlet.setInitOrder(1);
        servletContextHandler.addServlet(docServlet, "/doc/*");

        ServletHolder apiServlet = new ServletHolder("api", ServletContainer.class);
        apiServlet.setInitParameter("jersey.config.server.provider.packages", "org.transitclock.api.resources");
        apiServlet.setInitOrder(2);
        servletContextHandler.addServlet(apiServlet, "/api/v1/*");

        server.setHandler(servletContextHandler);
    }

    /**
     * Setup JSP Support for ServletContextHandlers.
     * <p>
     * NOTE: This is not required or appropriate if using a WebAppContext.
     * </p>
     *
     * @param servletContextHandler the ServletContextHandler to configure
     * @throws IOException if unable to configure
     */
    private void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException {
        // Establish Scratch directory for the servlet context (used by JSP compilation)
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists() && !scratchDir.mkdirs()) {
            throw new IOException("Unable to create scratch directory: " + scratchDir);
        }

        servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);

        // Set Classloader of Context to be sane (needed for JSTL)
        // JSP requires a non-System classloader, this simply wraps the
        // embedded System classloader in a way that makes it suitable
        // for JSP to use
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
        servletContextHandler.setClassLoader(jspClassLoader);

        // Manually call JettyJasperInitializer on context startup
        servletContextHandler.addBean(new EmbeddedJspStarter(servletContextHandler));

        // Create / Register JSP Servlet (must be named "jsp" per spec)
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("scratchdir", scratchDir.toString());
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "17");
        holderJsp.setInitParameter("compilerSourceVM", "17");
        holderJsp.setInitParameter("keepgenerated", "true");
        servletContextHandler
                .addServlet(holderJsp, "*.jsp");

        servletContextHandler.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
    }

    public void start() {
        try {
            server.start();
            logger.info("Go to http://localhost:" + 8080 + " in your browser");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException {
        URL indexUri = this.getClass().getResource(WEBROOT_INDEX);
        if (indexUri == null) {
            throw new FileNotFoundException("Unable to find resource " + WEBROOT_INDEX);
        }
        // Points to wherever /webroot/ (the resource) is
        return indexUri.toURI();
    }
}
