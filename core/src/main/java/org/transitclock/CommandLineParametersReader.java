package org.transitclock;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandLineParametersReader {
    /**
     * Parse and validate command line parameters. If the arguments is invalid the method uses {@code
     * System.exit()} to exit the application.
     */
    public static CommandLineParameters parseAndValidateCmdLine(String[] args) {
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
                jc.setProgramName("java -Xmx4G -jar transitime.jar");
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
