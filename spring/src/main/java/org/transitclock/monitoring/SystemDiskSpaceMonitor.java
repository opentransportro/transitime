/* (C)2023 */
package org.transitclock.monitoring;

import org.transitclock.config.LongConfigValue;
import org.transitclock.utils.EmailSender;
import org.transitclock.utils.StringUtils;

import java.io.File;

/**
 * Monitors to make sure there is sufficient disk space.
 *
 * @author SkiBu Smith
 */
public class SystemDiskSpaceMonitor extends MonitorBase {

    private LongConfigValue usableDiskSpaceThreshold = new LongConfigValue(
            "transitclock.monitoring.usableDiskSpaceThreshold",
            1024 * 1024 * 1024L, // ~1 GB
            "If usable disk space is less than this " + "value then file space monitoring is triggered.");

    private static LongConfigValue usableDiskSpaceThresholdGap = new LongConfigValue(
            "transitclock.monitoring.usableDiskSpaceThresholdGap",
            100 * 1024 * 1024L, // ~100 MB
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now above usableDiskSpaceThreshold + "
                    + "usableDiskSpaceThresholdGap ");

    /********************** Member Functions **************************/

    /**
     * Simple constructor
     *
     * @param emailSender
     * @param agencyId
     */
    public SystemDiskSpaceMonitor(EmailSender emailSender, String agencyId) {
        super(emailSender, agencyId);
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
                        + StringUtils.memoryFormat(usableDiskSpaceThreshold.getValue())
                        + ".",
                usableSpace);

        // Determine the threshold for triggering. If already triggered
        // then raise the threshold by usableDiskSpaceThresholdGap in order
        // to prevent lots of e-mail being sent out if the value is
        // dithering around usableDiskSpaceThreshold.
        long threshold = usableDiskSpaceThreshold.getValue();
        if (wasTriggered()) threshold += usableDiskSpaceThresholdGap.getValue();

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
