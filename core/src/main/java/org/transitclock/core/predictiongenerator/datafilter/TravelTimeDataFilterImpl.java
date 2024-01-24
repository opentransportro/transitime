/* (C)2023 */
package org.transitclock.core.predictiongenerator.datafilter;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.configData.PredictionConfig;
import org.transitclock.ipc.data.IpcArrivalDeparture;

/**
 * @author scrudden Filter by schedule adherence min and max travel time min and max
 */
@Slf4j
public class TravelTimeDataFilterImpl implements TravelTimeDataFilter {
    @Override
    public boolean filter(IpcArrivalDeparture departure, IpcArrivalDeparture arrival) {
        if (arrival != null && departure != null) {
            long traveltime = arrival.getTime().getTime() - departure.getTime().getTime();

            if (arrival.getScheduledAdherence() == null
                    || (departure.getScheduledAdherence() != null
                            && departure
                                    .getScheduledAdherence()
                                    .isWithinBounds(
                                            PredictionConfig.minSceheduleAdherence.getValue(), PredictionConfig.maxSceheduleAdherence.getValue()))) {
                // TODO Arrival schedule adherence appears not to be set much. So
                // only stop if set and outside range.
                if (arrival.getScheduledAdherence() == null
                        || arrival.getScheduledAdherence()
                                .isWithinBounds(PredictionConfig.minSceheduleAdherence.getValue(), PredictionConfig.maxSceheduleAdherence.getValue())) {
                    if (traveltime < PredictionConfig.maxTravelTimeAllowedInModel.getValue()
                            && traveltime > PredictionConfig.minTravelTimeAllowedInModel.getValue()) {
                        return false;

                    } else {
                        logger.warn(
                                "Travel time {} outside allowable range for {} to {}.", traveltime, departure, arrival);
                    }
                } else {
                    logger.warn("Schedule adherence outside allowable range. {}", arrival);
                }
            } else {
                logger.warn("Schedule adherence outside allowable range. {}", departure);
            }
        } else {
            logger.warn("Arrival and/or departure not set.");
        }

        return true;
    }
}
