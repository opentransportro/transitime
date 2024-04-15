package org.transitclock.config.data;

import org.transitclock.config.DoubleConfigValue;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.LongConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.utils.Time;

public class MonitoringConfig {

    public static final DoubleConfigValue maxQueueFraction = new DoubleConfigValue(
            "transitclock.monitoring.maxQueueFraction",
            0.4,
            "If database queue fills up by more than this 0.0 - 1.0 "
                    + "fraction then database monitoring is triggered.");

    public static final DoubleConfigValue maxQueueFractionGap = new DoubleConfigValue(
            "transitclock.monitoring.maxQueueFractionGap",
            0.1,
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now below maxQueueFraction - "
                    + "maxQueueFractionGap ");


    public static DoubleConfigValue minPredictableBlocks = new DoubleConfigValue(
            "transitclock.monitoring.minPredictableBlocks",
            0.50,
            "The minimum fraction of currently active blocks that " + "should have a predictable vehicle");

    public static DoubleConfigValue minPredictableBlocksGap = new DoubleConfigValue(
            "transitclock.monitoring.minPredictableBlocksGap",
            0.25,
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now above minPredictableBlocks + "
                    + "minPredictableBlocksGap ");

    public static IntegerConfigValue minimumPredictableVehicles = new IntegerConfigValue(
            "transitclock.monitoring.minimumPredictableVehicles",
            3,
            "When looking at small number of vehicles it is too easy "
                    + "to get below minimumPredictableBlocks. So number of "
                    + "predictable vehicles is increased to this amount if "
                    + "below when determining the fraction.");




    public static final DoubleConfigValue cpuThreshold = new DoubleConfigValue(
            "transitclock.monitoring.cpuThreshold",
            0.99,
            "If CPU load averaged over a minute exceeds this 0.0 - 1.0 " + "value then CPU monitoring is triggered.");

    public static final DoubleConfigValue cpuThresholdGap = new DoubleConfigValue(
            "transitclock.monitoring.cpuThresholdGap",
            0.1,
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now below cpuThreshold - "
                    + "cpuThresholdGap ");




    public static IntegerConfigValue allowableNoAvlSecs = new IntegerConfigValue(
            "transitclock.monitoring.allowableNoAvlSecs",
            5 * Time.SEC_PER_MIN,
            "How long in seconds that can not receive valid AVL data " + "before monitoring triggers an alert.");

    public static StringConfigValue avlFeedEmailRecipients = new StringConfigValue(
            "transitclock.monitoring.avlFeedEmailRecipients",
            "monitoring@transitclock.org",
            "Comma separated list of e-mail addresses indicating who "
                    + "should be e-mail when monitor state changes for AVL "
                    + "feed.");



    /**
     * Returns comma separated list of who should be notified via e-mail when trigger state changes
     * for the monitor. Specified by the Java property transitclock.monitoring.emailRecipients . Can
     * be overwritten by an implementation of a monitor if want different list for a monitor.
     *
     * @return E-mail addresses of who to notify
     */
    public String recipients() {
        return avlFeedEmailRecipients.getValue();
    }




    public static StringConfigValue emailRecipients = new StringConfigValue(
            "transitclock.monitoring.emailRecipients",
            "Comma separated list of e-mail addresses indicating who "
                    + "should be e-mailed when monitor state changes.");

    public static IntegerConfigValue retryTimeoutSecs = new IntegerConfigValue(
            "transitclock.monitoring.retryTimeoutSecs",
            5,
            "How long in seconds system should wait before rexamining "
                    + "monitor. This way a short lived outage can be ignored. "
                    + "0 seconds means do not retry.");




    public static final IntegerConfigValue secondsBetweenMonitorinPolling = new IntegerConfigValue(
            "transitclock.monitoring.secondsBetweenMonitorinPolling",
            120,
            "How frequently an monitoring should be run to look for " + "problems.");




    public static final LongConfigValue usableDiskSpaceThreshold = new LongConfigValue(
            "transitclock.monitoring.usableDiskSpaceThreshold",
            1024 * 1024 * 1024L, // ~1 GB
            "If usable disk space is less than this " + "value then file space monitoring is triggered.");

    public static final LongConfigValue usableDiskSpaceThresholdGap = new LongConfigValue(
            "transitclock.monitoring.usableDiskSpaceThresholdGap",
            100 * 1024 * 1024L, // ~100 MB
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now above usableDiskSpaceThreshold + "
                    + "usableDiskSpaceThresholdGap ");



    public static final LongConfigValue availableFreePhysicalMemoryThreshold = new LongConfigValue(
            "transitclock.monitoring.availableFreePhysicalMemoryThreshold",
            10 * 1024 * 1024L, // ~10 MB
            "If available free physical memory is less than this "
                    + "value then free memory monitoring is triggered. This should be "
                    + "relatively small since on Linux the operating system will use "
                    + "most of the memory for buffers and such when it is available. "
                    + "Therefore even when only a small amount of memory is available "
                    + "the system is still OK.");

    public static final LongConfigValue availableFreePhysicalMemoryThresholdGap = new LongConfigValue(
            "transitclock.monitoring.availableFreePhysicalMemoryThresholdGap",
            150 * 1024 * 1024L, // ~150 MB
            "When transitioning from triggered to untriggered don't "
                    + "want to send out an e-mail right away if actually "
                    + "dithering. Therefore will only send out OK e-mail if the "
                    + "value is now above availableFreePhysicalMemoryThreshold + "
                    + "availableFreePhysicalMemoryThresholdGap ");

}
