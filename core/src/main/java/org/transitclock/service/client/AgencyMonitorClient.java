/* (C)2023 */
package org.transitclock.service.client;

import java.rmi.RemoteException;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.db.webstructs.WebAgency;
import org.transitclock.service.contract.ConfigInterface;
import org.transitclock.service.contract.ServerStatusInterface;
import org.transitclock.service.ConfigServiceImpl;
import org.transitclock.service.ServerStatusServiceImpl;

/**
 * Makes the ServerStatusInterface.monitor() RMI call easy to access. Intended to be used on client,
 * such as a web page on a web server.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AgencyMonitorClient {

    public static String pingAgency(String agencyId) {
        String msg = null;

        ConfigInterface configInterface = ConfigServiceImpl.instance();
        if (configInterface == null) {
            msg = "Could not create ConfigInterface for RMI";
            logger.error(msg);
        } else {
            try {
                // Do the RMI call to make sure agency is running and can
                // communicate with it.
                configInterface.getAgencies();
            } catch (RemoteException e) {
                msg = "Could not connect via RMI. " + e.getMessage();
                logger.error(msg);
            }
        }

        return msg;
    }

    /**
     * Does a simple ping to each agency to make sure that the core for that agency is running and
     * can communicate with it via IPC.
     *
     * @return An error message if there is a problem, otherwise null
     */
    public static String pingAllAgencies() {
        String errorMessageForAllAgencies = "";
        Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
        for (WebAgency webAgency : webAgencies) {
            if (webAgency.isActive()) {
                // Actually do the low-level monitoring on the core system
                String errorMessage = pingAgency(webAgency.getAgencyId());
                if (errorMessage != null)
                    errorMessageForAllAgencies += "agencyId="
                            + webAgency.getAgencyId()
                            + " NOT AVAILABLE but marked as active. "
                            + errorMessage
                            + "; ";
            }
        }

        // Return error message if there is one. Otherwise return null.
        if (!errorMessageForAllAgencies.isEmpty()) return errorMessageForAllAgencies;
        else return null;
    }

    /**
     * Uses RMI to invoke monitoring of the agency core to see if everything is operating properly.
     * If there is a problem then and error message is returned. If everything OK then null
     * returned.
     *
     * @param agencyId Which agency to monitor
     * @return Error message if problem, or null
     */
    public static String monitor(String agencyId) {
        ServerStatusInterface serverStatusInterface = ServerStatusServiceImpl.instance();
        if (serverStatusInterface == null) {
            logger.error("Could not create ServerStatusInterface for RMI for " + "agencyId={}", agencyId);
            return null;
        }

        String resultStr;
        try {
            resultStr = serverStatusInterface.monitor();
            return resultStr;
        } catch (RemoteException e) {
            WebAgency webAgency = WebAgency.getCachedWebAgency(agencyId);
            logger.error("Exception when trying to monitor agency {}. {}", webAgency, e.getMessage());
            return "Could not connect via RMI for " + webAgency;
        }
    }

    /**
     * Goes through all agencies in the WebAgency database table that are marked as active and
     * monitor them to see if there are any problems. The monitoring on the server side will
     * automatically send out e-mail notifications if there is a problems. This method sends out
     * e-mails if there is a problem connecting to an agency.
     *
     * <p>Probably not needed if using MonitoringModule which already monitors an agency repeatedly.
     *
     * @return
     */
    public static String monitorAllAgencies() {
        String errorMessageForAllAgencies = "";
        Collection<WebAgency> webAgencies = WebAgency.getCachedOrderedListOfWebAgencies();
        for (WebAgency webAgency : webAgencies) {
            if (webAgency.isActive()) {
                // Actually do the low-level monitoring on the core system
                String errorMessage = monitor(webAgency.getAgencyId());
                if (errorMessage != null)
                    errorMessageForAllAgencies +=
                            "For agencyId=" + webAgency.getAgencyId() + ": " + errorMessage + "; ";
            }
        }

        // Return error message if there is one. Otherwise return null.
        if (!errorMessageForAllAgencies.isEmpty()) return errorMessageForAllAgencies;
        else return null;
    }
}
