/* (C)2023 */
package org.transitclock.monitoring;

import java.util.Date;
import java.util.List;
import org.transitclock.applications.Core;
import org.transitclock.core.BlocksInfo;
import org.transitclock.db.structs.Block;

/**
 * For monitoring active blocks. Unlike the other monitors, this one never triggers an alarm, it
 * simply posts metrics to cloudwatch
 */
public class ActiveBlocksMonitor extends MonitorBase {

    private long reportingIntervalInMillis = 60l * 1000l;

    private Date lastUpdate = new Date();

    public ActiveBlocksMonitor(String agencyId) {
        super(agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    @Override
    protected boolean triggered() {
        Date now = new Date();
        if (now.getTime() - lastUpdate.getTime() > reportingIntervalInMillis) {
            List<Block> blocks = BlocksInfo.getCurrentlyActiveBlocks();
            double activeBlockCount = (blocks != null ? blocks.size() : 0);
            double totalBlockCount = Core.getInstance().getDbConfig().getBlockCount();
            // cloudwatch metrics for active/total moved to PredictabilityMonitor
            double activeBlockCountPercentage = 0;
            if (activeBlockCount > 0) {
                activeBlockCountPercentage = activeBlockCount / totalBlockCount;
            }
            lastUpdate = new Date();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "Active Blocks";
    }
}
