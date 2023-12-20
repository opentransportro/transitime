/* (C)2023 */
package org.transitclock.ipc.rmi;

import org.transitclock.utils.Time;

import java.rmi.RemoteException;

/**
 * A sample RMI server class. Shows how RMI server class can be implemented.
 *
 * @author SkiBu Smith
 */
public class HelloServer extends AbstractServer implements Hello {

    private static HelloServer singleton = null;

    /********************** Member Functions **************************/

    /**
     * Constructor. Made private so that can only be instantiated by getInstance(). Doesn't actually
     * do anything since all the work is done in the superclass constructor.
     */
    private HelloServer(String projectId, String objectName) {
        super(projectId, objectName);
    }

    public static HelloServer getInstance(String projectId) {
        // Create the singleton if need be
        if (singleton == null) singleton = new HelloServer(projectId, Hello.class.getSimpleName());

        // Return it
        return singleton;
    }

    /* (non-Javadoc)
     * @see org.transitclock.rmi.Hello#concat(java.lang.String, java.lang.String)
     */
    @Override
    public String concat(String s1, String s2) throws RemoteException {
        // Sleep for a bit to simulate server getting bogged down
        Time.sleep(2000);

        return getAgencyId() + ": " + s1 + s2;
    }

    public static void main(String args[]) {
        String projectId = args.length > 0 ? args[0] : "testProjectId";
        HelloServer.getInstance(projectId);
    }
}
