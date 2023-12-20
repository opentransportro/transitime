/* (C)2023 */
package org.transitclock.configData;

import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;

/**
 * Config params for RMI
 *
 * @author SkiBu Smith
 */
public class RmiConfig {

    /**
     * For a client that needs to connect to an agency server. Usually would get RMI host name from
     * the WebAgencies table in the web database. But when doing simple calls using this parameter
     * eliminates the need to connect to the web database, speeding up testing.
     *
     * @return
     */
    public static String rmiHost() {
        return rmiHost.getValue();
    }

    private static StringConfigValue rmiHost = new StringConfigValue(
            "transitclock.rmi.rmiHost",
            null,
            "For a client that needs to connect to an agency server. "
                    + "When null system gets RMI host name from the WebAgencies "
                    + "table in the web database. But when doing simple calls "
                    + "using this parameter eliminates the need to connect to "
                    + "the web database, speeding up testing.");

    /**
     * Which port to use for RMI calls. Usually RMI uses port 1099 but using default of 2099 to not
     * interfere with other RMI based applications
     *
     * @return
     */
    public static int rmiPort() {
        return rmiPort.getValue();
    }

    private static IntegerConfigValue rmiPort = new IntegerConfigValue(
            "transitclock.rmi.rmiPort",
            2099,
            "Which port to use for RMI calls. Usually RMI uses port "
                    + "1099 but using default of 2099 to not interfere with "
                    + "other RMI based applications.");

    /**
     * Which secondary port to use for RMI calls, for once initial communication has been
     * established. Usually RMI uses port 0 which means any port. But then can't configure firewall
     * to limit access to specific ports. Therefore using default value of 2098 so that the port is
     * consistent. Every server on a machine must use a different secondary port for communication.
     *
     * @return
     */
    public static int secondaryRmiPort() {
        return secondaryRmiPort.getValue();
    }

    private static IntegerConfigValue secondaryRmiPort = new IntegerConfigValue(
            "transitclock.rmi.secondaryRmiPort",
            2098,
            "Which secondary port to use for RMI calls, for once "
                    + "initial communication has been established. Usually "
                    + "RMI uses port 0 which means any port. But then can't "
                    + "configure firewall to limit access to specific ports. "
                    + "Therefore using default value of 2098 so that the port "
                    + "is consistent. Every server on a machine must use a "
                    + "different secondary port for communication.");
}
