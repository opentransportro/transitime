/* (C)2023 */
package org.transitclock.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.ApplicationContext;
import org.transitclock.Module;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.MonitoringConfig;
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
@Slf4j
public class MonitoringModule extends Module {
    @Autowired
    private AgencyMonitor agencyMonitor;

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
        try {
            // Wait appropriate amount of time till poll again
            // Actually do the monitoring
            String resultStr = agencyMonitor.checkAll();
            if (resultStr != null) {
                logger.error("MonitoringModule detected problem. {}", resultStr);
            }
        } catch (Exception e) {
            logger.error("Errror in MonitoringModule for agencyId={}", AgencyConfig.getAgencyId(), e);
        }
    }

    @Override
    public int executionPeriod() {
        return MonitoringConfig.secondsBetweenMonitorinPolling.getValue() * Time.MS_PER_SEC;
    }

    @Override
    public ExecutionType getExecutionType() {
        return ExecutionType.FIXED_RATE;
    }
}
