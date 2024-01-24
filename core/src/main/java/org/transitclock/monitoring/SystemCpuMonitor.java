/* (C)2023 */
package org.transitclock.monitoring;

import java.util.Calendar;
import java.util.GregorianCalendar;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.configData.MonitoringConfig;
import org.transitclock.utils.StringUtils;
import org.transitclock.utils.Time;

/**
 * Monitors to make sure that server CPU is not too high.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class SystemCpuMonitor extends MonitorBase {

    public SystemCpuMonitor(String agencyId) {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    /**
     * Sees if recent CPU load is higher than value specified by cpuThreshold. Since CPU loads spike
     * this method checks a second time after a brief 1000msec sleep so can get an average CPU
     * value.
     *
     * @return True if CPU load higher than cpuThreshold. If CPU load lower or can't determine CPU
     *     load then returns false.
     */
    @Override
    protected boolean triggered() {
        // If just a few, 12, minutes past midnight then don't bother checking
        // CPU since that is when the log files are compressed and we always
        // get high CPU then. Note that need to use calendar for the default
        // timezone since that is what logging uses. Don't want to use timezone
        // for the agency because that is likely different.
        Calendar calendar = new GregorianCalendar();
        int secondsIntoDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60
                + calendar.get(Calendar.MINUTE) * 60
                + calendar.get(Calendar.SECOND);
        if (secondsIntoDay < 12 * Time.SEC_PER_MIN) return false;

        Object resultObject = SystemMemoryMonitor.getOperatingSystemValue("getSystemCpuLoad");
        if (resultObject != null) {
            double cpuLoad = (Double) resultObject;

            // If cpuLoad too high take another reading after a brief sleep
            // and take the average. This is important because sometimes the
            // CPU will be temporarily spiked at 1.0 and don't want to send
            // out an alert for short lived spikes.
            if (cpuLoad >= MonitoringConfig.cpuThreshold.getValue()) {
                logger.debug(
                        "CPU load was {} which is higher than threshold " + "of {} so taking another reading.",
                        StringUtils.twoDigitFormat(cpuLoad),
                        StringUtils.twoDigitFormat(MonitoringConfig.cpuThreshold.getValue()));
                Time.sleep(Time.MS_PER_MIN);
                resultObject = SystemMemoryMonitor.getOperatingSystemValue("getSystemCpuLoad");
                double cpuLoad2 = (Double) resultObject;

                // Take average of cpuLoad
                cpuLoad = (cpuLoad + cpuLoad2) / 2.0;
            }

            setMessage(
                    "CPU load is "
                            + StringUtils.twoDigitFormat(cpuLoad)
                            + " while limit is "
                            + StringUtils.twoDigitFormat(MonitoringConfig.cpuThreshold.getValue())
                            + ".",
                    cpuLoad);

            // Determine the threshold for triggering. If already triggered
            // then lower the threshold by cpuThresholdGap in order
            // to prevent lots of e-mail being sent out if the value is
            // dithering around cpuThreshold.
            double threshold = MonitoringConfig.cpuThreshold.getValue();
            if (wasTriggered()) threshold -= MonitoringConfig.cpuThresholdGap.getValue();

            // Return true if CPU problem found
            return cpuLoad >= threshold;
        }

        // Could not determine CPU load so have to return false
        return false;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "System CPU";
    }
}
