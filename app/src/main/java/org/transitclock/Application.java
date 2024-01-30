package org.transitclock;

import jakarta.servlet.DispatcherType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
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
import org.hibernate.Session;
import org.transitclock.api.EmbeddedJspStarter;
import org.transitclock.api.utils.ApiLoggingFilter;
import org.transitclock.api.utils.WebLoggingFilter;
import org.transitclock.api.utils.XSSFilter;
import org.transitclock.applications.Core;
import org.transitclock.applications.GtfsFileProcessor;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.core.dataCache.StopArrivalDepartureCacheFactory;
import org.transitclock.core.dataCache.TripDataHistoryCacheFactory;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.utils.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

@Slf4j
public class Application {
    private final CommandLineParameters cli;

    // Resource path pointing to where the WEBROOT is
    private static final String WEBROOT_INDEX = "/webroot/";
    private final Server server;


    public Application(CommandLineParameters commandLineParameters) throws IOException, URISyntaxException {
        this.cli = commandLineParameters;
        this.server = new Server();
    }

    public void loadGtfs() {
        if(cli.shouldLoadGtfs()) {
            GtfsFileProcessor processor = GtfsFileProcessor.createGtfsFileProcessor(cli);
            processor.process();
        }
    }

    public void run() {
        String agencyId = AgencyConfig.getAgencyId();
        try {
            try {
                populateCaches();
            } catch (Exception e) {
                logger.error("Failed to populate cache.", e);
            }

            // Close cache if shutting down.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Closing cache.");
                    CacheManagerFactory.getInstance().close();
                    logger.info("Cache closed.");
                } catch (Exception e) {
                    logger.error("Cache close failed...", e);
                }
            }));

            // Initialize the core now
            Core core = Core.createCore(agencyId);

            var serverInstance = createWebserver();
            serverInstance.start();
            logger.info("Go to http://localhost:" + cli.port + " in your browser");
            serverInstance.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void createApiKey() {
        ApiKeyManager.getInstance()
                .generateApiKey(
                        "Sean Og Crudden",
                        "http://www.transitclock.org",
                        "og.crudden@gmail.com",
                        "123456",
                        "foo");
    }

    public void createWebAgency() {
        WebAgency webAgency = new WebAgency(AgencyConfig.getAgencyId(),
                "127.0.0.1",
                true,
                DbSetupConfig.getDbName(),
                DbSetupConfig.getDbType(),
                "localhost",
                DbSetupConfig.getDbUserName(),
                DbSetupConfig.getDbPassword());

        // Store the WebAgency
        webAgency.store("web");
    }



    private void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        if (!CoreConfig.cacheReloadStartTimeStr.getValue().isEmpty() && !CoreConfig.cacheReloadEndTimeStr.getValue().isEmpty()) {
            if (TripDataHistoryCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating TripDataHistoryCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                TripDataHistoryCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (FrequencyBasedHistoricalAverageCache.getInstance() != null) {
                logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                FrequencyBasedHistoricalAverageCache.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (StopArrivalDepartureCacheFactory.getInstance() != null) {
                logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                StopArrivalDepartureCacheFactory.getInstance()
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }
            /*
            if(ScheduleBasedHistoricalAverageCache.getInstance()!=null)
            {
            	logger.debug("Populating ScheduleBasedHistoricalAverageCache cache for period {} to {}",cacheReloadStartTimeStr.getValue(),cacheReloadEndTimeStr.getValue());
            	ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, new Date(Time.parse(cacheReloadStartTimeStr.getValue()).getTime()), new Date(Time.parse(cacheReloadEndTimeStr.getValue()).getTime()));
            }
            */
        } else {
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (TripDataHistoryCacheFactory.getInstance() != null) {
                    logger.debug("Populating TripDataHistoryCache cache for period {} to {}", startDate, endDate);
                    TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                if (FrequencyBasedHistoricalAverageCache.getInstance() != null) {
                    logger.debug(
                            "Populating FrequencyBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    FrequencyBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }

            endDate = Calendar.getInstance().getTime();

            /* populate one day at a time to avoid memory issue */
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);
                if (StopArrivalDepartureCacheFactory.getInstance() != null) {
                    logger.debug("Populating StopArrivalDepartureCache cache for period {} to {}", startDate, endDate);
                    StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
            endDate = Calendar.getInstance().getTime();

            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (ScheduleBasedHistoricalAverageCache.getInstance() != null) {
                    logger.debug(
                            "Populating ScheduleBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    ScheduleBasedHistoricalAverageCache.getInstance().populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
        }
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

    public Server createWebserver() throws IOException, URISyntaxException {
        // Define ServerConnector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(cli.port);
        server.addConnector(connector);

        // Base URI for servlet context
        URI baseUri = getWebRootResourceUri();

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setErrorHandler(new ErrorHandler());
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

        return server;
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
