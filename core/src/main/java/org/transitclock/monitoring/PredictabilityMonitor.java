/* (C)2023 */
package org.transitclock.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.transitclock.SingletonContainer;
import org.transitclock.config.data.MonitoringConfig;
import org.transitclock.core.BlocksInfo;
import org.transitclock.core.dataCache.VehicleDataCache;
import org.transitclock.domain.structs.Block;
import org.transitclock.utils.StringUtils;

/**
 * Monitors how many vehicles are predictable compared to how many active blocks there currently
 * are.
 *
 * @author SkiBu Smith
 */
public class PredictabilityMonitor extends MonitorBase {

    private final VehicleDataCache vehicleDataCache = SingletonContainer.getInstance(VehicleDataCache.class);

    public PredictabilityMonitor(String agencyId) {
        super(agencyId);
    }

    /**
     * Returns the fraction (0.0 - 1.0) of the blocks that currently have a predictable vehicle
     * associated.
     *
     * @param threshold So can provide a more complete message if below the threshold
     * @return Fraction of blocks that have a predictable vehicle
     */
    private double fractionBlocksPredictable(double threshold) {
        // For creating message
        List<Block> activeBlocksWithoutVehicle = new ArrayList<Block>();
        // Determine number of currently active blocks.
        // If there are no currently active blocks then don't need to be
        // getting AVL data so return 0
        List<Block> activeBlocks = BlocksInfo.getCurrentlyActiveBlocks();
        if (activeBlocks.isEmpty()) {
            setMessage("No currently active blocks so predictability " + "considered to be OK.");
            return 1.0;
        }

        // Determine number of currently active vehicles
        int predictableVehicleCount = 0;
        for (Block block : activeBlocks) {
            // Determine vehicles associated with the block if there are any
            Collection<String> vehicleIdsForBlock =
                    vehicleDataCache.getVehiclesByBlockId(block.getId());
            predictableVehicleCount += vehicleIdsForBlock.size();

            // Keep track of active blocks without vehicles
            if (vehicleIdsForBlock.isEmpty()) {
                activeBlocksWithoutVehicle.add(block);
            }
        }

        // Determine fraction of active blocks that have a predictable vehicle
        double fraction = ((double) Math.max(predictableVehicleCount, MonitoringConfig.minimumPredictableVehicles.getValue()))
                / activeBlocks.size();

        // Provide simple message explaining the situation
        String message = "Predictable blocks fraction="
                + StringUtils.twoDigitFormat(fraction)
                + ", minimum allowed fraction="
                + StringUtils.twoDigitFormat(MonitoringConfig.minPredictableBlocks.getValue())
                + ", active blocks="
                + activeBlocks.size()
                + ", predictable vehicles="
                + predictableVehicleCount
                + " (minimumPredictableVehicles="
                + Math.max(predictableVehicleCount, MonitoringConfig.minimumPredictableVehicles.getValue())
                + ").";

        // If below the threshold then add all the active block IDs to the
        // message so can more easily see
        if (fraction < threshold) {
            StringBuilder sb = new StringBuilder();
            sb.append(" Currently active blocks without vehicles: ");
            for (Block block : activeBlocksWithoutVehicle) {
                sb.append("block=")
                        .append(block.getId())
                        .append(", serviceId=")
                        .append(block.getServiceId())
                        .append("; ");
            }
            message += sb.toString();
        }

        setMessage(message, fraction);

        // Return fraction of blocks that have a predictable vehicle
        return fraction;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    @Override
    protected boolean triggered() {
        // Determine the threshold for triggering. If already triggered
        // then raise the threshold by minPredictableBlocksGap in order
        // to prevent lots of e-mail being sent out if the value is
        // dithering around minPredictableBlocks.
        double threshold = MonitoringConfig.minPredictableBlocks.getValue();
        if (wasTriggered()) threshold += MonitoringConfig.minPredictableBlocksGap.getValue();

        double fraction = fractionBlocksPredictable(threshold);

        return fraction < threshold;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "Predictability";
    }
}
