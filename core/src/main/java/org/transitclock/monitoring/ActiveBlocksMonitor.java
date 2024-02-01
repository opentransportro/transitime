/* (C)2023 */
package org.transitclock.monitoring;

import java.util.Date;
import java.util.List;
import org.transitclock.Core;
import org.transitclock.SingletonContainer;
import org.transitclock.core.BlocksInfo;
import org.transitclock.domain.structs.Block;
import org.transitclock.gtfs.DbConfig;

/**
 * For monitoring active blocks. Unlike the other monitors, this one never triggers an alarm, it
 * simply posts metrics to cloudwatch
 */
public class ActiveBlocksMonitor extends MonitorBase {

    private final long reportingIntervalInMillis = 60L * 1000L;

    private Date lastUpdate = new Date();
    private final DbConfig dbConfig = SingletonContainer.getInstance(DbConfig.class);

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
            double totalBlockCount = dbConfig.getBlockCount();
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
