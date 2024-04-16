/* (C)2023 */
package org.transitclock.core.prediction.datafilter;

import org.transitclock.config.LongConfigValue;
import org.transitclock.properties.PredictionProperties;
import org.transitclock.service.dto.IpcArrivalDeparture;

import lombok.extern.slf4j.Slf4j;

/**
 * @author scrudden Filter by schedule adherence min and max dwell tiime min and max
 */
@Slf4j
public class DwellTimeDataFilterImpl implements DwellTimeDataFilter {
    private static final LongConfigValue minDwellTimeAllowedInModel = new LongConfigValue(
            "transitclock.prediction.dwell.minDwellTimeAllowedInModel",
            (long) 0,
            "Min dwell time to be considered in algorithm.");

    private final PredictionProperties predictionProperties;

    public DwellTimeDataFilterImpl(PredictionProperties predictionProperties) {
        this.predictionProperties = predictionProperties;
    }

    @Override
    public boolean filter(IpcArrivalDeparture arrival, IpcArrivalDeparture departure) {
        if (arrival != null && departure != null) {
            long dwelltime = departure.getTime().getTime() - arrival.getTime().getTime();

            if (departure.getScheduledAdherence() != null
                    && departure
                            .getScheduledAdherence()
                            .isWithinBounds(predictionProperties.getRls().getMinSceheduleAdherence(), predictionProperties.getRls().getMaxSceheduleAdherence())) {
                // TODO Arrival schedule adherence appears not to be set much. So
                // only stop if set and outside range.
                if (arrival.getScheduledAdherence() == null
                        || arrival.getScheduledAdherence()
                                .isWithinBounds(predictionProperties.getRls().getMinSceheduleAdherence(), predictionProperties.getRls().getMaxSceheduleAdherence())) {
                    if (dwelltime < predictionProperties.getRls().getMaxDwellTimeAllowedInModel()
                            && dwelltime > minDwellTimeAllowedInModel.getValue()) {
                        return false;
                    } else {
                        logger.warn("Dwell time {} outside allowable range for {}.", dwelltime, departure);
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
