/* (C)2023 */
package org.transitclock.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.utils.StringUtils;

/**
 * For monitoring CPU, available memory, and available disk space.
 *
 * <p>Note: Linux will use a great deal of RAM for caching and such when memory is available.
 * Therefore most of the time the free memory will be quite low. But this is OK since the operating
 * system will give up the RAM being used for caching and such if a process needs it. Therefore the
 * allowable free memory should be set to quite a low value.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class SystemMemoryMonitor extends MonitorBase {

    public SystemMemoryMonitor(String agencyId) {
        super(agencyId);
    }

    /**
     * Gets an operating system value via reflection. Yes, this is a rather obtuse way of getting
     * such values but it appears to work.
     *
     * @param methodName Name of the special internal com.sun.management.OperatingSystemMXBean
     *     method to call
     * @return The result from invoking the specified method
     */
    public static Object getOperatingSystemValue(String methodName) {
        OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            // Get the getSystemCpuLoad() method using reflection
            Method method = operatingSystemMxBean.getClass().getMethod(methodName);

            // Need to declare the method as accessible so that can
            // invoke it
            method.setAccessible(true);

            // Get and return the result by invoking the specified method
            return method.invoke(operatingSystemMxBean);
        } catch (Exception e) {
            logger.error("Could not execute " + "OperatingSystemMXBean.{}(). {}", methodName, e.getMessage());
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    /**
     * Sees if recent available memory is lower than value specified by
     * availableFreePhysicalMemoryThreshold.
     *
     * @return True if available memory is lower than availableFreePhysicalMemoryThreshold. If
     *     available memory is higher or can't determine available memory then returns false.
     */
    @Override
    protected boolean triggered() {
        Object resultObject = getOperatingSystemValue("getFreePhysicalMemorySize");
        if (resultObject != null) {
            long freePhysicalMemory = (Long) resultObject;

            // Provide message explaining situation
            setMessage(
                    "Free physical memory is "
                            + StringUtils.memoryFormat(freePhysicalMemory)
                            + " while the limit is "
                            + StringUtils.memoryFormat(MonitoringConfig.availableFreePhysicalMemoryThreshold.getValue())
                            + ".",
                    freePhysicalMemory);

            // Determine the threshold for triggering. If already triggered
            // then raise the threshold by availableFreePhysicalMemoryThresholdGap
            // in order to prevent lots of e-mail being sent out if the value
            // is dithering around availableFreePhysicalMemoryThreshold.
            long threshold = MonitoringConfig.availableFreePhysicalMemoryThreshold.getValue();
            if (wasTriggered()) {
                threshold += MonitoringConfig.availableFreePhysicalMemoryThresholdGap.getValue();
            }

            // Return true if problem detected
            return freePhysicalMemory < threshold;
        }

        // Could not determine available memory so have to return false
        return false;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "System Memory";
    }
}
