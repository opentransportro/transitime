/* (C)2023 */
package org.transitclock.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For monitoring whether the core system is working properly. For calling all of the specific
 * monitoring functions.
 *
 * @author SkiBu Smith
 */
public class AgencyMonitor {

    // List of all the monitoring to do
    private final List<MonitorBase> monitors;

    // For being able to reuse AgencyMonitors. This is important because
    // each monitor maintains state, such as if notification e-mail sent out.
    // Keyed on agencyId.
    private static final Map<String, AgencyMonitor> agencyMonitorMap = new HashMap<String, AgencyMonitor>();

    private static final Logger logger = LoggerFactory.getLogger(AgencyMonitor.class);

    private static final String enableSystemMonitoring = System.getProperty("transitclock.enableSystemMonitoring");

    /********************** Member Functions **************************/

    /**
     * Constructor declared private so have to use getInstance() to get an AgencyMonitor. Like a
     * singleton, but one AgencyMonitor for every agencyId.
     *
     * @param agencyId
     */
    private AgencyMonitor(String agencyId) {
        // Create all the monitors and add them to the monitors list
        monitors = new ArrayList<MonitorBase>();
        monitors.add(new AvlFeedMonitor(agencyId));
        monitors.add(new PredictabilityMonitor(agencyId));
        monitors.add(new DatabaseQueueMonitor(agencyId));
        monitors.add(new ActiveBlocksMonitor(agencyId));
        if (enableSystemMonitoring != null && enableSystemMonitoring.equalsIgnoreCase("true")) {
            monitors.add(new SystemMemoryMonitor(agencyId));
            monitors.add(new SystemCpuMonitor(agencyId));
            monitors.add(new SystemDiskSpaceMonitor(agencyId));
            monitors.add(new DatabaseMonitor(agencyId));
        }
    }

    /**
     * Returns the AgencyMonitor for the specified agencyId. If the AgencyMonitor for that agency
     * hasn't been created yet it is created. This is important because each monitor maintains
     * state, such as if notification e-mail sent out.
     *
     * @param agencyId Which agency get AgencyMonitor for
     * @return The AgencyMonitor for the agencyId
     */
    public static AgencyMonitor getInstance(String agencyId) {
        synchronized (agencyMonitorMap) {
            AgencyMonitor agencyMonitor = agencyMonitorMap.get(agencyId);
            if (agencyMonitor == null) {
                agencyMonitor = new AgencyMonitor(agencyId);
                agencyMonitorMap.put(agencyId, agencyMonitor);
            }
            return agencyMonitor;
        }
    }

    /**
     * Checks all the monitors for the agency and returns all resulting messages whether a monitor
     * is triggered or not. Useful for showing current status of system.
     *
     * @return List of results of monitoring
     */
    public List<MonitorResult> getMonitorResults() {
        // Check all the monitors, which will set their message
        checkAll();

        // For all the monitors return the results
        List<MonitorResult> monitorResults = new ArrayList<MonitorResult>();
        for (MonitorBase monitor : monitors) {
            MonitorResult monitorResult = new MonitorResult(monitor.type(), monitor.getMessage());
            monitorResults.add(monitorResult);
        }
        return monitorResults;
    }

    /**
     * Checks the core system to make sure it is working properly. If it is then null is returned.
     * If there are any problems then returns the concatenation of all the error messages. Sends out
     * notification e-mails if there is an issue via MonitorBase class. To be called periodically
     * via a MonitoringModule or via Inter Process Communication.
     *
     * @return Null if system OK, or the concatenation of the error message for all the monitoring
     *     if there are any problems.
     */
    public String checkAll() {
        logger.info("Monitoring agency for problems...");

        String errorMessage = "";

        // Check all the monitors.
        for (MonitorBase monitor : monitors) {
            if (monitor.checkAndNotify()) errorMessage += " " + monitor.getMessage();
        }

        // Return the concatenated error message if there were any
        if (errorMessage.length() > 0) return errorMessage;
        else return null;
    }

    public static void main(String[] args) {
        String agencyId = "mbta";
        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);
        String resultStr = agencyMonitor.checkAll();
        System.out.println("resultStr=" + resultStr);
    }
}
