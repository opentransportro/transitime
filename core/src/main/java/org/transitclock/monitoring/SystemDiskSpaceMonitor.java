/* (C)2023 */
package org.transitclock.monitoring;

import java.io.File;

import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.utils.StringUtils;

/**
 * Monitors to make sure there is sufficient disk space.
 *
 * @author SkiBu Smith
 */
public class SystemDiskSpaceMonitor extends MonitorBase {
    public SystemDiskSpaceMonitor(String agencyId) {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    /**
     * Checks whether file system getting too full, beyond usableDiskSpaceThreshold.
     *
     * @return True if file system getting too full
     */
    @Override
    protected boolean triggered() {
        long usableSpace = new File("/").getUsableSpace();

        // Provide message explaining situation
        setMessage(
                "Usable disk space is "
                        + StringUtils.memoryFormat(usableSpace)
                        + " while the minimum limit is "
                        + StringUtils.memoryFormat(MonitoringConfig.usableDiskSpaceThreshold.getValue())
                        + ".",
                usableSpace);

        // Determine the threshold for triggering. If already triggered
        // then raise the threshold by usableDiskSpaceThresholdGap in order
        // to prevent lots of e-mail being sent out if the value is
        // dithering around usableDiskSpaceThreshold.
        long threshold = MonitoringConfig.usableDiskSpaceThreshold.getValue();
        if (wasTriggered()) {
            threshold += MonitoringConfig.usableDiskSpaceThresholdGap.getValue();
        }

        // Return true if usable disk space problem found
        return usableSpace < threshold;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "System Disk Space";
    }
}
