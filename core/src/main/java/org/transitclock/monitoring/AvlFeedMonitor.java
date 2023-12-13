/* (C)2023 */
package org.transitclock.monitoring;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.config.IntegerConfigValue;
import org.transitclock.config.StringConfigValue;
import org.transitclock.core.AvlProcessor;
import org.transitclock.core.BlocksInfo;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.EmailSender;
import org.transitclock.utils.Time;

/**
 * For determining if the AVL feed is up. If not getting data when blocks are active then the AVL
 * feed is considered down. It is important to only expect data when blocks are active because
 * otherwise would always get false positives.
 *
 * @author SkiBu Smith
 */
public class AvlFeedMonitor extends MonitorBase {

    private CloudwatchService cloudwatchService;

    private static IntegerConfigValue allowableNoAvlSecs = new IntegerConfigValue(
            "transitclock.monitoring.allowableNoAvlSecs",
            5 * Time.SEC_PER_MIN,
            "How long in seconds that can not receive valid AVL data " + "before monitoring triggers an alert.");

    private static StringConfigValue avlFeedEmailRecipients = new StringConfigValue(
            "transitclock.monitoring.avlFeedEmailRecipients",
            "monitoring@transitclock.org",
            "Comma separated list of e-mail addresses indicating who "
                    + "should be e-mail when monitor state changes for AVL "
                    + "feed.");

    private static final Logger logger = LoggerFactory.getLogger(AvlFeedMonitor.class);

    /********************** Member Functions **************************/

    /**
     * Simple constructor
     *
     * @param emailSender
     * @param agencyId
     */
    public AvlFeedMonitor(CloudwatchService cloudwatchService, EmailSender emailSender, String agencyId) {
        super(emailSender, agencyId);
        this.cloudwatchService = cloudwatchService;
    }

    /**
     * Checks GPS time of last AVL report from the AVL feed. If it is recent, as specified by
     * transitclock.monitoring.allowableAvlFeedTimeNoDataSecs, then this method returns 0. If no GPS
     * data or the data is too old then returns age of last AVL report in seconds.
     *
     * @return 0 if have recent valid GPS data or age of last AVL report, in seconds.
     */
    private int avlFeedOutageSecs() {
        // Determine age of AVL report
        long lastAvlReportTime = AvlProcessor.getInstance().lastAvlReportTime();
        long ageOfAvlReport = System.currentTimeMillis() - lastAvlReportTime;
        Double ageOfAvlReportInSecs = new Double(ageOfAvlReport / Time.MS_PER_SEC);
        cloudwatchService.saveMetric(
                "PredictionLatestAvlReportAgeInSeconds",
                ageOfAvlReportInSecs,
                1,
                CloudwatchService.MetricType.AVERAGE,
                CloudwatchService.ReportingIntervalTimeUnit.MINUTE,
                false);

        logger.debug(
                "When monitoring AVL feed last AVL report={}",
                AvlProcessor.getInstance().getLastAvlReport());

        setMessage(
                "Last valid AVL report was "
                        + ageOfAvlReport / Time.MS_PER_SEC
                        + " secs old while allowable age is "
                        + allowableNoAvlSecs.getValue()
                        + " secs as specified by "
                        + "parameter "
                        + allowableNoAvlSecs.getID()
                        + " .",
                ageOfAvlReport / Time.MS_PER_SEC);

        if (ageOfAvlReport > allowableNoAvlSecs.getValue() * Time.MS_PER_SEC) {
            // last AVL report is too old
            return (int) (ageOfAvlReport / Time.MS_PER_SEC);
        } else {
            // Last AVL report is not too old
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    @Override
    protected boolean triggered() {
        // Check AVL feed
        int avlFeedOutageSecs = avlFeedOutageSecs();
        return avlFeedOutageSecs != 0;
    }

    /**
     * Returns true if there are no currently active blocks indicating that it doesn't matter if not
     * getting AVL data.
     *
     * @return true if no currently active blocks
     */
    @Override
    protected boolean acceptableEvenIfTriggered() {
        List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
        if (activeBlocks.size() == 0) {
            setAcceptableEvenIfTriggeredMessage("No currently active blocks " + "so AVL feed considered to be OK.");
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "AVL feed";
    }

    /**
     * Returns comma separated list of who should be notified via e-mail when trigger state changes
     * for the monitor. Specified by the Java property transitclock.monitoring.emailRecipients . Can
     * be overwritten by an implementation of a monitor if want different list for a monitor.
     *
     * @return E-mail addresses of who to notify
     */
    protected String recipients() {
        return avlFeedEmailRecipients.getValue();
    }
}
