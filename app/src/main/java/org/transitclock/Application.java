package org.transitclock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.transitclock.config.CRLFLogConverter;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.utils.threading.UncaughtExceptionHandler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({
    ApplicationProperties.class
})
@RequiredArgsConstructor
public class Application implements ApplicationRunner {

    private final ApplicationProperties properties;


    @SneakyThrows
    public static void main(String[] args) {
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
    }


    private void loadGtfs(CommandLineParameters cli) {
        GtfsFileProcessor processor = GtfsFileProcessor.createGtfsFileProcessor(cli);
        processor.process(properties.getGtfs());
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
