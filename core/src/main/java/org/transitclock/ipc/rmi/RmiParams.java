/* (C)2023 */
package org.transitclock.ipc.rmi;

import lombok.Getter;
import lombok.Setter;
import org.transitclock.configData.RmiConfig;

/**
 * For defining port numbers to be used for RMI
 *
 * @author SkiBu Smith
 */
public class RmiParams {
    // Usually will use special port 2099 in case another app is using
    // standard RMI port 1099
    @Getter
    @Setter
    public static int rmiPort = RmiConfig.rmiPort();

    // For secondary communication. Usually RMI uses port 0, which means any old
    // random port, but to deal with firewalls it is better to use a fixed port.
    @Getter
    @Setter
    public static int secondaryRmiPort = RmiConfig.secondaryRmiPort();
}
