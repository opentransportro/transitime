/* (C)2023 */
package org.transitclock.applications;

import java.io.PrintWriter;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.transitclock.config.ConfigFileReader;
import org.transitclock.db.webstructs.ApiKey;
import org.transitclock.db.webstructs.ApiKeyManager;

/**
 * For creating a new API key.
 *
 * @author Sean Crudden
 */
public class CreateAPIKey {
    /**
     * For testing and debugging. Currently creates a new key for an application.
     *
     * @param args
     */
    public static void main(String[] args) {

        Options options = new Options();
        Option helpOption = new Option("h", "help", false, "Display usage and help info.");

        Option configOption = new Option("c", "config", true, "Specifies optional configuration file to read in.");
        Option nameOption = new Option("n", "name", true, "Application Name");
        Option urlOption = new Option("u", "url", true, "Application URL");
        Option emailOption = new Option("e", "email", true, "Email address");
        Option phoneOption = new Option("p", "phone", true, "Phone number");
        Option descriptionOption = new Option("d", "description", true, "Description");
        options.addOption(configOption);
        options.addOption(nameOption);
        options.addOption(emailOption);
        options.addOption(urlOption);
        options.addOption(phoneOption);
        options.addOption(descriptionOption);
        options.addOption(helpOption);

        // Parse the options
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("n")
                    && cmd.hasOption("u")
                    && cmd.hasOption("e")
                    && cmd.hasOption("p")
                    && cmd.hasOption("d")) {
                String configFile = null;

                // Read in the data from config file
                configFile = cmd.getOptionValue("c");
                if (configFile != null) ConfigFileReader.processConfig(configFile);

                ApiKeyManager manager = ApiKeyManager.getInstance();
                ApiKey apiKey = manager.generateApiKey(
                        cmd.getOptionValue("n"),
                        cmd.getOptionValue("u"),
                        cmd.getOptionValue("e"),
                        cmd.getOptionValue("p"),
                        cmd.getOptionValue("d"));

                System.out.println(apiKey);
            } else {
                throw new Exception("All arguments required");
            }

        } catch (Exception e) {

            e.printStackTrace(System.out);
            printHelp(options);
        }
    }

    static void printHelp(Options options) {
        final String commandLineSyntax = "java -jar createApiKey.jar";
        final PrintWriter writer = new PrintWriter(System.out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                80, // printedRowWidth
                commandLineSyntax,
                "args:", // header
                options,
                2, // spacesBeforeOption
                2, // spacesBeforeOptionDescription
                null, // footer
                true); // displayUsage
        writer.close();
    }
}
