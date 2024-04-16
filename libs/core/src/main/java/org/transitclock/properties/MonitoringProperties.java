package org.transitclock.properties;

import lombok.Data;

@Data
public class MonitoringProperties {
    // config param: transitclock.monitoring.maxQueueFraction
    // If database queue fills up by more than this 0.0 - 1.0 fraction then database monitoring is triggered.
    private Double maxQueueFraction = 0.4;

    // config param: transitclock.monitoring.maxQueueFractionGap
    // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now below maxQueueFraction - maxQueueFractionGap
    private Double maxQueueFractionGap = 0.1;

    // config param: transitclock.monitoring.minPredictableBlocks
    // The minimum fraction of currently active blocks that should have a predictable vehicle
    private Double minPredictableBlocks = 0.5;

    // config param: transitclock.monitoring.minPredictableBlocksGap
    // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above minPredictableBlocks + minPredictableBlocksGap
    private Double minPredictableBlocksGap = 0.25;

    // config param: transitclock.monitoring.minimumPredictableVehicles
    // When looking at small number of vehicles it is too easy to get below minimumPredictableBlocks. So number of predictable vehicles is increased to this amount if below when determining the fraction.
    private Integer minimumPredictableVehicles = 3;

    // config param: transitclock.monitoring.cpuThreshold
    // If CPU load averaged over a minute exceeds this 0.0 - 1.0 value then CPU monitoring is triggered.
    private Double cpuThreshold = 0.99;

    // config param: transitclock.monitoring.cpuThresholdGap
    // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now below cpuThreshold - cpuThresholdGap
    private Double cpuThresholdGap = 0.1;

    // config param: transitclock.monitoring.allowableNoAvlSecs
    // How long in seconds that can not receive valid AVL data before monitoring triggers an alert.
    private Integer allowableNoAvlSecs = 300;

    // config param: transitclock.monitoring.avlFeedEmailRecipients
    // Comma separated list of e-mail addresses indicating who should be e-mail when monitor state changes for AVL feed.
    private String avlFeedEmailRecipients = "monitoring@transitclock.org";

    // config param: transitclock.monitoring.emailRecipients
    // Comma separated list of e-mail addresses indicating who should be e-mailed when monitor state changes.
    private String emailRecipients = null;

    // config param: transitclock.monitoring.retryTimeoutSecs
    // How long in seconds system should wait before rexamining monitor. This way a short lived outage can be ignored. 0 seconds means do not retry.
    private Integer retryTimeoutSecs = 5;

    // config param: transitclock.monitoring.secondsBetweenMonitorinPolling
    // How frequently an monitoring should be run to look for problems.
    private Integer secondsBetweenMonitorinPolling = 120;

    // config param: transitclock.monitoring.usableDiskSpaceThreshold
    // If usable disk space is less than this value then file space monitoring is triggered.
    private Long usableDiskSpaceThreshold = 1073741824L;

    // config param: transitclock.monitoring.usableDiskSpaceThresholdGap
    // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above usableDiskSpaceThreshold + usableDiskSpaceThresholdGap
    private Long usableDiskSpaceThresholdGap = 104857600L;

    // config param: transitclock.monitoring.availableFreePhysicalMemoryThreshold
    // If available free physical memory is less than this value then free memory monitoring is triggered. This should be relatively small since on Linux the operating system will use most of the memory for buffers and such when it is available. Therefore even when only a small amount of memory is available the system is still OK.
    private Long availableFreePhysicalMemoryThreshold = 10485760L;

    // config param: transitclock.monitoring.availableFreePhysicalMemoryThresholdGap
    // When transitioning from triggered to untriggered don't want to send out an e-mail right away if actually dithering. Therefore will only send out OK e-mail if the value is now above availableFreePhysicalMemoryThreshold + availableFreePhysicalMemoryThresholdGap
    private Long availableFreePhysicalMemoryThresholdGap = 157286400L;

}
