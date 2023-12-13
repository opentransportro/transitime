/* (C)2023 */
package org.transitclock.ipc.rmi;

import org.transitclock.configData.RmiConfig;

/**
 * For defining port numbers to be used for RMI
 *
 * @author SkiBu Smith
 */
public class RmiParams {
    // Usually will use special port 2099 in case another app is using
    // standard RMI port 1099
    public static int rmiPort = RmiConfig.rmiPort();

    // For secondary communication. Usually RMI uses port 0, which means any old
    // random port, but to deal with firewalls it is better to use a fixed port.
    public static int secondaryRmiPort = RmiConfig.secondaryRmiPort();

    public static int getRmiPort() {
        return rmiPort;
    }

    public static void setRmiPort(int newRmiPort) {
        rmiPort = newRmiPort;
    }

    public static int getSecondaryRmiPort() {
        return secondaryRmiPort;
    }

    public static void setSecondaryRmiPort(int newSecondaryRmiPort) {
        secondaryRmiPort = newSecondaryRmiPort;
    }
}
