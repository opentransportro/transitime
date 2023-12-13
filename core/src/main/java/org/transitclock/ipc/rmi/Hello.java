/* (C)2023 */
package org.transitclock.ipc.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A test interface for showing how can work with RMI
 *
 * @author SkiBu Smith
 */
public interface Hello extends Remote {
    public String concat(String s1, String s2) throws RemoteException;
}
