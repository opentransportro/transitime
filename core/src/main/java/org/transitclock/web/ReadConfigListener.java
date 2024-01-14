/* (C)2023 */
package org.transitclock.web;

/**
 * To be called when webapp starts up so that the configuration xml or properties file is read in.
 * The webapp web.xml file should be configured to associate this class as a listener so that
 * contextInitialized() is automatically called at webapp startup. And the initialization parameter
 * transitime_config_file_location needs to be set to specify the config file to read in.
 *
 * <p>When running Tomcat within Eclipse it is most likely easiest to just set the necessary Java
 * properties in the Debug Configurations VM properties.
 *
 * <p>This class in in the main transitime package so that it can be used by all webapps.
 *
 * @author Sean Crudden
 */
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.transitclock.config.ConfigFileReader;

public class ReadConfigListener implements ServletContextListener {
    private static final String FILE_LOCATION_PARAM_NAME = "transitime_config_file_location";

    /**
     * Doesn't need to do anything since this class is only for reading on configuration properties
     * at initialization.
     *
     * @param arg0
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {}

    /**
     * Called at initialization. Reads in config properties.
     *
     * @param arg0
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {

        try {
            String configFileLocation = arg0.getServletContext().getInitParameter(FILE_LOCATION_PARAM_NAME);

            System.out.println("Reading from web.xml file the context-param name "
                    + FILE_LOCATION_PARAM_NAME
                    + " from the web.xml file to "
                    + "determine the name of the Java properties configuration "
                    + "file.");

            // If not params file configured in web.xml complain
            if (configFileLocation == null) {
                System.err.println("Should set the context-param "
                        + FILE_LOCATION_PARAM_NAME
                        + " in the web.xml file to specify Transitime "
                        + "config file to read in.");
                return;
            }

            // Process the params file
            ConfigFileReader.processConfig(configFileLocation);
        } catch (Exception e) {
            // Output error if params file could not be read in
            e.printStackTrace();
        }
    }
}
