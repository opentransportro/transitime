package org.transitclock;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.transitclock.config.CRLFLogConverter;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.CoreConfig;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.core.dataCache.*;
import org.transitclock.core.dataCache.frequency.FrequencyBasedHistoricalAverageCache;
import org.transitclock.core.dataCache.scheduled.ScheduleBasedHistoricalAverageCache;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.domain.hibernate.HibernateUtils;
import org.transitclock.domain.webstructs.WebAgency;
import org.transitclock.utils.Time;
import org.transitclock.utils.threading.UncaughtExceptionHandler;

import java.io.FileNotFoundException;
import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import static org.transitclock.utils.ApplicationShutdownSupport.addShutdownHook;

@Slf4j
@Getter
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ApplicationProperties.class})
public class Application implements ApplicationRunner {
    private static final String WEBROOT_INDEX = "/webroot/";

    private final Environment env;
    private final FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache;
    private final ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache;
    private final TripDataHistoryCacheInterface tripDataHistoryCacheInterface;
    private final StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface;
    private final DwellTimeModelCacheInterface dwellTimeModelCacheInterface;
    private final CacheManager cacheManager;
    private final ApiKeyManager apiKeyManager;

    public Application(Environment env, FrequencyBasedHistoricalAverageCache frequencyBasedHistoricalAverageCache, ScheduleBasedHistoricalAverageCache scheduleBasedHistoricalAverageCache, TripDataHistoryCacheInterface tripDataHistoryCacheInterface, StopArrivalDepartureCacheInterface stopArrivalDepartureCacheInterface, DwellTimeModelCacheInterface dwellTimeModelCacheInterface, CacheManager cacheManager, ApiKeyManager apiKeyManager) {
        this.env = env;
        this.frequencyBasedHistoricalAverageCache = frequencyBasedHistoricalAverageCache;
        this.scheduleBasedHistoricalAverageCache = scheduleBasedHistoricalAverageCache;
        this.tripDataHistoryCacheInterface = tripDataHistoryCacheInterface;
        this.stopArrivalDepartureCacheInterface = stopArrivalDepartureCacheInterface;
        this.dwellTimeModelCacheInterface = dwellTimeModelCacheInterface;
        this.cacheManager = cacheManager;
        this.apiKeyManager = apiKeyManager;
    }


    @SneakyThrows
    public static void main(String[] args) {
        TimeZone aDefault = TimeZone.getDefault();
        logger.warn("Application started using Timezone [{}, offset={}, daylight={}]", aDefault.getID(), aDefault.getRawOffset(), aDefault.useDaylightTime());
        var uncaughtExceptionHandler = new UncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

        var currentThread = Thread.currentThread();
        currentThread.setName("main");
        currentThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);

        ConfigFileReader.processConfig();
        var application = new SpringApplication(Application.class);
        ConfigurableEnvironment environment = application.run(args).getEnvironment();
        logApplicationStartup(environment);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String applicationName = env.getProperty("spring.application.name");
        String serverPort = Optional.ofNullable(env.getProperty("server.port")).orElse("8080");
        String contextPath = Optional
            .ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(StringUtils::isNotBlank)
            .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("The host name could not be determined, using `localhost` as fallback");
        }
        logger.info(
            CRLFLogConverter.CRLF_SAFE_MARKER,
            """

            ----------------------------------------------------------
            \tApplication '{}' is running! Access URLs:
            \tLocal: \t\t{}://localhost:{}{}
            \tExternal: \t{}://{}:{}{}
            \tProfile(s): \t{}
            ----------------------------------------------------------""",
            applicationName,
            protocol,
            serverPort,
            contextPath,
            protocol,
            hostAddress,
            serverPort,
            contextPath,
            env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var cli = parseAndValidateCmdLine(args.getSourceArgs());

        if(cli.shouldLoadGtfs()) {
            loadGtfs(cli);
        }
        createApiKey();
        createWebAgency();
        run(cli);
    }


    private void loadGtfs(CommandLineParameters cli) {
        GtfsFileProcessor processor = GtfsFileProcessor.createGtfsFileProcessor(cli);
        processor.process();
    }

    private void run(CommandLineParameters cli) {
        try {
            try {
                populateCaches();
            } catch (Exception e) {
                logger.error("Failed to populate cache.", e);
            }

            addShutdownHook("close-cache", () -> {
                try {
                    logger.info("Closing cache.");
                    cacheManager.close();
                    logger.info("Cache closed.");
                } catch (Exception e) {
                    logger.error("Cache close failed...", e);
                }
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void createApiKey() {
        try {
            apiKeyManager
                    .generateApiKey(
                            "Sean Og Crudden",
                            "http://www.transitclock.org",
                            "og.crudden@gmail.com",
                            "123456",
                            "foo");
        } catch (IllegalArgumentException ignored) {

        }
    }

    private void createWebAgency() {
        String agencyId = AgencyConfig.getAgencyId();
        WebAgency webAgency = new WebAgency(agencyId,
                "127.0.0.1",
                true,
                DbSetupConfig.getDbName(),
                DbSetupConfig.getDbType(),
                DbSetupConfig.getDbHost(),
                DbSetupConfig.getDbUserName(),
                DbSetupConfig.getDbPassword());

        try {
            // Store the WebAgency
            webAgency.store(agencyId);
        } catch (IllegalArgumentException ignored) {

        }
    }

    private void populateCaches() throws Exception {
        Session session = HibernateUtils.getSession();

        Date endDate = Calendar.getInstance().getTime();

        if (!CoreConfig.cacheReloadStartTimeStr.getValue().isEmpty() && !CoreConfig.cacheReloadEndTimeStr.getValue().isEmpty()) {
            if (tripDataHistoryCacheInterface != null) {
                logger.debug(
                        "Populating TripDataHistoryCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                tripDataHistoryCacheInterface
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (frequencyBasedHistoricalAverageCache != null) {
                logger.debug(
                        "Populating FrequencyBasedHistoricalAverageCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                frequencyBasedHistoricalAverageCache
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }

            if (stopArrivalDepartureCacheInterface != null) {
                logger.debug(
                        "Populating StopArrivalDepartureCache cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                stopArrivalDepartureCacheInterface
                        .populateCacheFromDb(
                                session,
                                new Date(Time.parse(CoreConfig.cacheReloadStartTimeStr.getValue()).getTime()),
                                new Date(Time.parse(CoreConfig.cacheReloadEndTimeStr.getValue()).getTime())
                        );
            }
            if (dwellTimeModelCacheInterface != null) {
                logger.debug(
                        "Populating DwellTimeModelCacheInterface cache for period {} to {}",
                        CoreConfig.cacheReloadStartTimeStr.getValue(),
                        CoreConfig.cacheReloadEndTimeStr.getValue());
                dwellTimeModelCacheInterface
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

                if (tripDataHistoryCacheInterface != null) {
                    logger.debug("Populating TripDataHistoryCache cache for period {} to {}", startDate, endDate);
                    tripDataHistoryCacheInterface.populateCacheFromDb(session, startDate, endDate);
                }

                if (frequencyBasedHistoricalAverageCache != null) {
                    logger.debug(
                            "Populating FrequencyBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    frequencyBasedHistoricalAverageCache.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }

            endDate = Calendar.getInstance().getTime();

            /* populate one day at a time to avoid memory issue */
            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);
                if (stopArrivalDepartureCacheInterface != null) {
                    logger.debug("Populating StopArrivalDepartureCache cache for period {} to {}", startDate, endDate);
                    stopArrivalDepartureCacheInterface.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
            endDate = Calendar.getInstance().getTime();

            for (int i = 0; i < CoreConfig.getDaysPopulateHistoricalCache(); i++) {
                Date startDate = DateUtils.addDays(endDate, -1);

                if (scheduleBasedHistoricalAverageCache != null) {
                    logger.debug(
                            "Populating ScheduleBasedHistoricalAverageCache cache for period {} to" + " {}",
                            startDate,
                            endDate);
                    scheduleBasedHistoricalAverageCache.populateCacheFromDb(session, startDate, endDate);
                }

                endDate = startDate;
            }
        }
    }
//
//    /**
//     * Setup JSP Support for ServletContextHandlers.
//     * <p>
//     * NOTE: This is not required or appropriate if using a WebAppContext.
//     * </p>
//     *
//     * @param servletContextHandler the ServletContextHandler to configure
//     * @throws IOException if unable to configure
//     */
//    private void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException {
//        // Establish Scratch directory for the servlet context (used by JSP compilation)
//        File tempDir = new File(System.getProperty("java.io.tmpdir"));
//        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");
//
//        if (!scratchDir.exists() && !scratchDir.mkdirs()) {
//            throw new IOException("Unable to create scratch directory: " + scratchDir);
//        }
//
//        servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);
//
//        // Set Classloader of Context to be sane (needed for JSTL)
//        // JSP requires a non-System classloader, this simply wraps the
//        // embedded System classloader in a way that makes it suitable
//        // for JSP to use
//        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
//        servletContextHandler.setClassLoader(jspClassLoader);
//
//        // Manually call JettyJasperInitializer on context startup
//        servletContextHandler.addBean(new EmbeddedJspStarter(servletContextHandler));
//
//        // Create / Register JSP Servlet (must be named "jsp" per spec)
//        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
//        holderJsp.setInitOrder(0);
//        holderJsp.setInitParameter("scratchdir", scratchDir.toString());
//        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
//        holderJsp.setInitParameter("fork", "false");
//        holderJsp.setInitParameter("xpoweredBy", "false");
//        holderJsp.setInitParameter("compilerTargetVM", "17");
//        holderJsp.setInitParameter("compilerSourceVM", "17");
//        holderJsp.setInitParameter("keepgenerated", "true");
//        servletContextHandler
//                .addServlet(holderJsp, "*.jsp");
//
//        servletContextHandler.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
//    }
//
//    private Server createWebserver(CommandLineParameters cli) throws IOException, URISyntaxException {
//        QueuedThreadPool jettyThreadpool = new QueuedThreadPool(10, 3, 30000);
//        jettyThreadpool.setName("jetty-threadpool");
//        Server server = new Server(jettyThreadpool);
//        ServerConnector connector = new ServerConnector(server);
//        connector.setPort(cli.port);
//        server.addConnector(connector);
//        server.setStopAtShutdown(true);
//
//        // Base URI for servlet context
//        URI baseUri = getWebRootResourceUri();
//
//        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        servletContextHandler.setErrorHandler(new ErrorHandler());
//        servletContextHandler.setContextPath("/");
//        servletContextHandler.addFilter(new XSSFilter(), "/*", EnumSet.allOf(DispatcherType.class));
//        servletContextHandler.addFilter(new ApiLoggingFilter(), "/api/*", EnumSet.allOf(DispatcherType.class));
//        servletContextHandler.addFilter(new WebLoggingFilter(), "/*", EnumSet.allOf(DispatcherType.class));
//        servletContextHandler.setBaseResourceAsString(baseUri.toASCIIString());
//
//        enableEmbeddedJspSupport(servletContextHandler);
//
//        // Default Servlet (always last, always named "default")
//        ServletHolder holderDefault = new ServletHolder("default", DefaultServlet.class);
//        holderDefault.setInitParameter("resourceBase", baseUri.toASCIIString());
//        holderDefault.setInitParameter("dirAllowed", "true");
//        holderDefault.setInitOrder(0);
//        servletContextHandler.addServlet(holderDefault, "/");`
//
//        ServletHolder docServlet = new ServletHolder("doc", ServletContainer.class);
//        docServlet.setInitParameter("jersey.config.server.provider.packages", "io.swagger.v3.jaxrs2.integration.resources,org.transitclock.api.resources,org.transitclock.api.utils");
//        docServlet.setInitOrder(0);
//        servletContextHandler.addServlet(docServlet, "/doc/*");
//
//        ServletHolder apiServlet = new ServletHolder("api", ServletContainer.class);
//        apiServlet.setInitParameter("jersey.config.server.provider.packages", "org.transitclock.api.resources");
//        apiServlet.setInitOrder(0);
//        servletContextHandler.addServlet(apiServlet, "/api/v1/*");
//
//        server.setHandler(servletContextHandler);
//
//        return server;
//    }

    private URI getWebRootResourceUri() throws FileNotFoundException, URISyntaxException {
        URL indexUri = this.getClass().getResource(WEBROOT_INDEX);
        if (indexUri == null) {
            throw new FileNotFoundException("Unable to find resource " + WEBROOT_INDEX);
        }
        // Points to wherever /webroot/ (the resource) is
        return indexUri.toURI();
    }

    private static CommandLineParameters parseAndValidateCmdLine(String[] args) {
        CommandLineParameters params = new CommandLineParameters();
        try {
            // It is tempting to use JCommander's command syntax: http://jcommander.org/#_more_complex_syntaxes_commands
            // But this seems to lead to confusing switch ordering and more difficult subsequent use of the
            // parsed commands, since there will be three separate objects.
            JCommander jc = JCommander.newBuilder()
                    .addObject(params)
                    .args(args)
                    .build();

            if (params.version) {
                System.exit(0);
            }

            if (params.help) {
                jc.setProgramName("java -Xmx4G -jar transitclock.jar");
                jc.usage();
                System.exit(0);
            }
            params.inferAndValidate();
        } catch (ParameterException pex) {
            logger.error("Parameter error: {}", pex.getMessage());
            System.exit(1);
        }
        return params;
    }
}
