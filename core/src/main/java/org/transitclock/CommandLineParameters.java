package org.transitclock;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Lists;
import org.transitclock.utils.Time;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a JCommander-annotated class that holds parameters for OTP stand-alone mode. These
 * parameters can be parsed from the command line, or provided in a file using Jcommander's
 * at-symbol syntax (see http://jcommander.org/#Syntax). When stand-alone OTP is started as a
 * daemon, parameters are loaded from such a file, located by default in
 * '/etc/opentripplanner.cfg'.
 * <p>
 * Note that JCommander-annotated parameters can be any type that can be constructed from a string.
 * This module also contains classes for validating parameters. See:
 * http://jcommander.org/#Parameter_validation
 * <p>
 * Some parameter fields are not initialized so when inferring other parameters, we can check for
 * null and see whether they were specified on the command line.
 */
public class CommandLineParameters {
    private static final String TIP = " Use --help to see available options.";
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CACHE_PATH = "/var/transitime/cache";
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    /* Options for the command itself, rather than build or server sub-tasks. */
    @Parameter(names = {"--help"}, help = true, description = "Print this help message and exit.")
    public boolean help;

    @Parameter(names = {"--version"}, description = "Print the version, and then exit.")
    public boolean version = false;


    // GTFS LOADING
    @Parameter(names = {"--gtfs-url"}, description = "")
    public String gtfsUrl;

    @Parameter(names = {"--gtfs-zip"}, description = "")
    public String gtfsZip;

    @Parameter(names = {"--gtfs-path-offset-distance"})
    public double gtfsPathOffsetDistance = 0;

    @Parameter(names = {"--gtfs-max-stop-to-path-distance"})
    public double gtfsMaxStopToPathDistance = 60;

    @Parameter(names = {"--gtfs-max-distance-between-stops"})
    public double gtfsMaxDistanceBetweenStops = 6000;

    @Parameter(names = {"--gtfs-disable-special-loopback-to-beginning-case"}, description = "")
    public boolean gtfsDisableSpecialLoopBackToBeginningCase = false;

    @Parameter(names = {"--gtfs-max-distance-for-eliminating-vertices"}, description = "")
    public double gtfsMaxDistanceForEliminatingVertices = 3;

    @Parameter(names = {"--gtfs-default-wait-time-at-stop"}, description = "")
    public int gtfsDefaultWaitTimeAtStop = 10* Time.MS_PER_SEC;

    @Parameter(names = {"--gtfs-max-speed"}, description = "")
    public double gtfsMaxSpeed = 97.0;

    @Parameter(names = {"--gtfs-max-travel-time-segment-length"}, description = "")
    public double gtfsMaxTravelTimeSegmentLength = 600.0;

    @Parameter(names = {"--gtfs-trim-path-before-first-stop-of-trip"}, description = "")
    public boolean gtfsTrimPathBeforeFirstStopOfTrip = false;




    // API KEY
    @Parameter(names = {"--create-api-key"}, description = "")
    public boolean createApiKey;

    // WEB AGENCY for web ui
    @Parameter(names = {"--create-web-agency"}, description = "")
    public String createWebAgency;


    @Parameter(names = {"--cache"}, validateWith = ReadWriteDirectory.class, description = "")
    public File cacheDirectory = new File(DEFAULT_CACHE_PATH);

    /* Options for the server sub-task. */
    @Parameter(names = {"--serve"}, description = "Run an OTP API server.")
    public boolean serve = false;

    @Parameter(names = {"--bindAddress"}, description = "Specify which network interface to bind to by address. 0.0.0.0 means all interfaces.")
    public String bindAddress = DEFAULT_BIND_ADDRESS;

    @Parameter(names = {"--port"}, validateWith = PositiveInteger.class, description = "Server port for plain HTTP.")
    public Integer port = DEFAULT_PORT;

    /**
     * The remaining single parameter after the switches is the directory with the configuration
     * files. This directory may contain other files like the graph, input data and report files.
     */
    @Parameter(validateWith = ReadableDirectory.class, description = "/inputs/directory")
    public List<File> baseDirectory = Lists.newArrayList(new File(System.getProperty("user.dir") + "/config"));

    public static CommandLineParameters createCliForTest(File baseDir) {
        CommandLineParameters params = new CommandLineParameters();
        params.baseDirectory = List.of(baseDir);
        return params;
    }

    /**
     * Set some convenience parameters based on other parameters' values. Default values are validated
     * even when no command line option is specified, and we will not bind ports unless a server is
     * started. Therefore we only validate that port parameters are positive integers, and we check
     * that ports are available only when a server will be started.
     */
    public void inferAndValidate() {
        validateOneDirectorySet();
        validatePortsAvailable();
        validateParameterCombinations();
    }

    /**
     * Workaround for bug https://github.com/cbeust/jcommander/pull/390 The main non-switch parameter
     * has to be a list. Return the first one.
     */
    public File getBaseDirectory() {
        validateOneDirectorySet();
        return baseDirectory.get(0);
    }

    public File getRelativeDirectory(String path) {
        String baseDir = getBaseDirectory().getAbsolutePath();
        String subDir = baseDir + (baseDir.endsWith("/") ? "" : File.separator) + path;
        File directory = new File(subDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        if(!directory.isDirectory()) {
            throw new RuntimeException("The requested path is not a directory");
        }

        return directory;
    }

    /**
     * @param port a port that we plan to bind to
     * @throws ParameterException if that port is not available
     */
    private static void checkPortAvailable(int port) throws ParameterException {
        ServerSocket socket = null;
        boolean portUnavailable = false;
        String reason = null;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            portUnavailable = true;
            reason = e.getMessage();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // will not be thrown
                }
            }
        }
        if (portUnavailable) {
            String msg = String.format(": port %d is not available. %s.", port, reason);
            throw new ParameterException(msg);
        }
    }

    private void validateOneDirectorySet() {
        if (baseDirectory == null || baseDirectory.size() != 1) {
            throw new ParameterException("You must supply a single directory name.");
        }
    }

    private void validatePortsAvailable() {
        if (serve) {
            checkPortAvailable(port);
        }
    }

    private void validateParameterCombinations() {
//        List<String> cmds = listParams(
//                List.of("--load", "--build", "--loadStreet", "--buildStreet"),
//                List.of(load, build, loadStreet, buildStreet)
//        );
//
//        if (cmds.isEmpty()) {
//            throw new ParameterException("Nothing to do." + TIP);
//        }
//        if (cmds.size() != 1) {
//            throw new ParameterException(String.join(", ", cmds) + " can not be used together." + TIP);
//        }
//        if (load) {
//            validateParamNotSet("--load", save, "--save");
//        }
//        if (buildStreet) {
//            validateParamNotSet("--buildStreet", serve, "--serve");
//        }
    }

    private void validateParamNotSet(String mainParam, boolean noneCompliantParam, String name) {
        if (noneCompliantParam) {
            throw new ParameterException(mainParam + " can not be used with " + name + TIP);
        }
    }


    private List<String> listParams(List<String> names, List<Boolean> parms) {
        List<String> cmds = new ArrayList<>();
        for (int i = 0; i < parms.size(); ++i) {
            if (parms.get(i)) {
                cmds.add(names.get(i));
            }
        }
        return cmds;
    }

    public boolean shouldLoadGtfs() {
        return gtfsZip != null || gtfsUrl != null;
    }

    public static class ReadableDirectory implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            File file = new File(value);
            if (!file.isDirectory()) {
                String msg = String.format("%s: '%s' is not a directory.", name, value);
                throw new ParameterException(msg);
            }
            if (!file.canRead()) {
                String msg = String.format("%s: directory '%s' is not readable.", name, value);
                throw new ParameterException(msg);
            }
        }
    }

    public static class ReadWriteDirectory implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            new ReadableDirectory().validate(name, value);
            File file = new File(value);
            if (!file.canWrite()) {
                String msg = String.format("%s: directory '%s' is not writable.", name, value);
                throw new ParameterException(msg);
            }
        }
    }

    public static class PositiveInteger implements IParameterValidator {

        @Override
        public void validate(String name, String value) throws ParameterException {
            int i = Integer.parseInt(value);
            if (i <= 0) {
                String msg = String.format("%s must be a positive integer.", name);
                throw new ParameterException(msg);
            }
        }
    }
}
