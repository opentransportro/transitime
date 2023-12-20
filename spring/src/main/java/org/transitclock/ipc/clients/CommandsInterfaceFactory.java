/* (C)2023 */
package org.transitclock.ipc.clients;

import java.util.HashMap;
import java.util.Map;
import org.transitclock.ipc.interfaces.CommandsInterface;
import org.transitclock.ipc.rmi.ClientFactory;

/**
 * Provides a CommandsInterface client that can be sent commands.
 *
 * @author SkiBu Smith
 */
public class CommandsInterfaceFactory {

    // Keyed by agencyId
    private static Map<String, CommandsInterface> commandsInterfaceMap = new HashMap<String, CommandsInterface>();

    /********************** Member Functions **************************/

    /**
     * Gets the singleton instance.
     *
     * @param agencyId
     * @return
     */
    public static CommandsInterface get(String agencyId) {
        CommandsInterface commandsInterface = commandsInterfaceMap.get(agencyId);
        if (commandsInterface == null) {
            commandsInterface = ClientFactory.getInstance(agencyId, CommandsInterface.class);
            commandsInterfaceMap.put(agencyId, commandsInterface);
        }

        return commandsInterface;
    }
}
