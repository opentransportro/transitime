/* (C)2023 */
package org.transitclock.monitoring;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.core.avl.AvlProcessor;
import org.transitclock.core.avl.assigner.BlockInfoProvider;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.gtfs.DbConfig;

/**
 * For monitoring whether the core system is working properly. For calling all of the specific
 * monitoring functions.
 *
 * @author SkiBu Smith
 */
@Slf4j
@Component
public class AgencyMonitor {

    // List of all the monitoring to do
    private final List<MonitorBase> monitors;

    private static final String enableSystemMonitoring = System.getProperty("transitclock.enableSystemMonitoring");

    /**
     * Constructor declared private so have to use getInstance() to get an AgencyMonitor. Like a
     * singleton, but one AgencyMonitor for every agencyId.
     */
    public AgencyMonitor(BlockInfoProvider blockInfoProvider,
                         DbConfig dbConfig,
                         DataDbLogger dataDbLogger,
                         VehicleDataCache vehicleDataCache,
                         AvlProcessor avlProcessor) {
        String agencyId = AgencyConfig.getAgencyId();
        // Create all the monitors and add them to the monitors list
        monitors = new ArrayList<>();
        monitors.add(new AvlFeedMonitor(agencyId, dataDbLogger, avlProcessor, blockInfoProvider));
        monitors.add(new PredictabilityMonitor(agencyId, dataDbLogger, vehicleDataCache, blockInfoProvider));
        monitors.add(new DatabaseQueueMonitor(agencyId, dataDbLogger));
        monitors.add(new ActiveBlocksMonitor(agencyId, dataDbLogger, blockInfoProvider, dbConfig));
        if (enableSystemMonitoring != null && enableSystemMonitoring.equalsIgnoreCase("true")) {
            monitors.add(new SystemMemoryMonitor(agencyId, dataDbLogger));
            monitors.add(new SystemCpuMonitor(agencyId, dataDbLogger));
            monitors.add(new SystemDiskSpaceMonitor(agencyId, dataDbLogger));
            monitors.add(new DatabaseMonitor(agencyId, dataDbLogger));
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
        if (!errorMessage.isEmpty())
            return errorMessage;

        return null;
    }
}
