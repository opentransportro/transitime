package org.transitclock;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.core.dataCache.ehcache.CacheManagerFactory;

@Slf4j
public class TransitclockMain {
    @SneakyThrows
    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        ConfigFileReader.processConfig();
        CommandLineParameters cli = parseAndValidateCmdLine(args);

        CacheManager cm = CacheManagerFactory.getInstance();
        SingletonContainer.registerInstance(CacheManager.class, cm);

        startTransitClock(cli);
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
//                System.out.println("transitime " + projectInfo().getVersionString());
                System.exit(0);
            }

            if (params.help) {
//                System.out.println("transitime " + projectInfo().getVersionString());
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

    @SneakyThrows
    private static void startTransitClock(CommandLineParameters cli) {
        var application = new Application(cli);
        application.loadGtfs();
        application.createApiKey();
        application.createWebAgency();
        application.run();
    }
}
