/* (C)2023 */
package org.transitclock.monitoring;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.core.AvlProcessor;
import org.transitclock.core.BlocksInfo;
import org.transitclock.db.structs.Block;
import org.transitclock.utils.Time;

/**
 * For determining if the AVL feed is up. If not getting data when blocks are active then the AVL
 * feed is considered down. It is important to only expect data when blocks are active because
 * otherwise would always get false positives.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class AvlFeedMonitor extends MonitorBase {

    public AvlFeedMonitor(String agencyId) {
        super(agencyId);
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
        Double ageOfAvlReportInSecs = (double) (ageOfAvlReport / Time.MS_PER_SEC);

        logger.debug(
                "When monitoring AVL feed last AVL report={}",
                AvlProcessor.getInstance().getLastAvlReport());

        setMessage(
                "Last valid AVL report was "
                        + ageOfAvlReport / Time.MS_PER_SEC
                        + " secs old while allowable age is "
                        + MonitoringConfig.allowableNoAvlSecs.getValue()
                        + " secs as specified by "
                        + "parameter "
                        + MonitoringConfig.allowableNoAvlSecs.getID()
                        + " .",
                ageOfAvlReport / Time.MS_PER_SEC);

        if (ageOfAvlReport > MonitoringConfig.allowableNoAvlSecs.getValue() * Time.MS_PER_SEC) {
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
        if (activeBlocks.isEmpty()) {
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
}
