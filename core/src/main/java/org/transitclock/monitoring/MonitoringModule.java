/* (C)2023 */
package org.transitclock.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.transitclock.Module;
import org.transitclock.config.data.AgencyConfig;
import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.utils.Time;

import java.util.concurrent.TimeUnit;

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
@Component
public class MonitoringModule implements Module {
    private final AgencyMonitor agencyMonitor;

    public MonitoringModule(AgencyMonitor agencyMonitor) {
        this.agencyMonitor = agencyMonitor;
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Scheduled(fixedRateString = "${transitclock.monitoring.secondsBetweenMonitorinPolling:120}", timeUnit = TimeUnit.SECONDS)
    public void run() {
        // Wait appropriate amount of time till poll again
        // Actually do the monitoring
        String resultStr = agencyMonitor.checkAll();
        if (resultStr != null) {
            logger.error("MonitoringModule detected problem. {}", resultStr);
        }
    }
}
