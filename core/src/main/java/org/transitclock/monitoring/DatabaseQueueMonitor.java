/* (C)2023 */
package org.transitclock.monitoring;

import org.transitclock.Core;
import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.domain.hibernate.DataDbLogger;
import org.transitclock.utils.StringUtils;

/**
 * For monitoring access to database. Examines size of the db logging queue to make sure that writes
 * are not getting backed up.
 *
 * @author SkiBu Smith
 */
public class DatabaseQueueMonitor extends MonitorBase {


    /**
     * Simple constructor
     *
     * @param agencyId
     */
    public DatabaseQueueMonitor(String agencyId) {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    @Override
    protected boolean triggered() {
        Core core = Core.getInstance();
        if (core == null) return false;

        DataDbLogger dbLogger = core.getDbLogger();

        setMessage(
                "Database queue fraction="
                        + StringUtils.twoDigitFormat(dbLogger.queueLevel())
                        + " while max allowed fraction="
                        + StringUtils.twoDigitFormat(MonitoringConfig.maxQueueFraction.getValue())
                        + ", and items in queue="
                        + dbLogger.queueSize()
                        + ".",
                dbLogger.queueLevel());

        // Determine the threshold for triggering. If already triggered
        // then lower the threshold by maxQueueFractionGap in order
        // to prevent lots of e-mail being sent out if the value is
        // dithering around maxQueueFraction.
        double threshold = MonitoringConfig.maxQueueFraction.getValue();
        if (wasTriggered()) threshold -= MonitoringConfig.maxQueueFractionGap.getValue();

        return dbLogger.queueLevel() > threshold;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "Database Queue";
    }
}
