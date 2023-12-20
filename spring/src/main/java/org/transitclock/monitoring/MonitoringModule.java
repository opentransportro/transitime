/* (C)2023 */
package org.transitclock.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.Module;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

/**
 * A module that runs in a separate thread and repeatedly uses AgencyMonitor to monitor a core
 * project to determine if there are any problems. Since AgencyMonitor is used notification e-mails
 * are automatically sent.
 *
 * <p>To use with a core project use:
 * -Dtransitclock.modules.optionalModulesList=org.transitclock.monitor.MonitoringModule
 *
 * @author SkiBu Smith
 */
public class MonitoringModule extends Module {

    private static IntegerConfigValue secondsBetweenMonitorinPolling = new IntegerConfigValue(
            "transitclock.monitoring.secondsBetweenMonitorinPolling",
            120,
            "How frequently an monitoring should be run to look for " + "problems.");

    private static final Logger logger = LoggerFactory.getLogger(MonitoringModule.class);

    /********************** Member Functions **************************/

    /**
     * @param agencyId
     */
    public MonitoringModule(String agencyId) {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Log that module successfully started
        logger.info("Started module {} for agencyId={}", getClass().getName(), getAgencyId());

        AgencyMonitor agencyMonitor = AgencyMonitor.getInstance(agencyId);

        // Run forever. Sleep before monitoring since don't want to monitor
        // immediately at startup
        IntervalTimer timer = new IntervalTimer();
        while (true) {
            try {
                // Wait appropriate amount of time till poll again
                long elapsedMsec = timer.elapsedMsec();
                long sleepTime = secondsBetweenMonitorinPolling.getValue() * Time.MS_PER_SEC - elapsedMsec;
                if (sleepTime < 0) {
                    logger.warn("For monitoring module upposed to have a polling "
                            + "rate of "
                            + secondsBetweenMonitorinPolling.getValue() * Time.MS_PER_SEC
                            + " msec but processing previous data took "
                            + elapsedMsec
                            + " msec so polling again immediately.");
                } else {
                    Time.sleep(sleepTime);
                }
                timer.resetTimer();

                // Actually do the monitoring
                String resultStr = agencyMonitor.checkAll();

                if (resultStr != null) {
                    logger.error("MonitoringModule detected problem. {}", resultStr);
                }
            } catch (Exception e) {
                logger.error("Errror in MonitoringModule for agencyId={}", AgencyConfig.getAgencyId(), e);
            }
        }
    }
}
