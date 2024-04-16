/* (C)2023 */
package org.transitclock.core.prediction.datafilter;

import org.transitclock.properties.PredictionProperties;
import org.transitclock.service.dto.IpcArrivalDeparture;

import lombok.extern.slf4j.Slf4j;

/**
 * @author scrudden Filter by schedule adherence min and max travel time min and max
 */
@Slf4j
public class TravelTimeDataFilterImpl implements TravelTimeDataFilter {
    private final PredictionProperties predictionProperties;

    public TravelTimeDataFilterImpl(PredictionProperties predictionProperties) {
        this.predictionProperties = predictionProperties;
    }

    @Override
    public boolean filter(IpcArrivalDeparture departure, IpcArrivalDeparture arrival) {
        if (arrival != null && departure != null) {
            long traveltime = arrival.getTime().getTime() - departure.getTime().getTime();

            if (arrival.getScheduledAdherence() == null
                    || (departure.getScheduledAdherence() != null
                            && departure
                                    .getScheduledAdherence()
                                    .isWithinBounds(
                                            predictionProperties.getRls().getMinSceheduleAdherence(), predictionProperties.getRls().getMaxSceheduleAdherence()))) {
                // TODO Arrival schedule adherence appears not to be set much. So
                // only stop if set and outside range.
                if (arrival.getScheduledAdherence() == null
                        || arrival.getScheduledAdherence()
                                .isWithinBounds(predictionProperties.getRls().getMinSceheduleAdherence(), predictionProperties.getRls().getMaxSceheduleAdherence())) {
                    if (traveltime < predictionProperties.getTravel().getMaxTravelTimeAllowedInModel()
                            && traveltime > predictionProperties.getTravel().getMinTravelTimeAllowedInModel()) {
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
