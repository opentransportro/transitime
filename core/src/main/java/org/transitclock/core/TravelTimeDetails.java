/* (C)2023 */
package org.transitclock.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeDataFilter;
import org.transitclock.core.predictiongenerator.datafilter.TravelTimeFilterFactory;
import org.transitclock.service.dto.IpcArrivalDeparture;

@Slf4j
@Getter
public class TravelTimeDetails {
    private final IpcArrivalDeparture departure;
    private final IpcArrivalDeparture arrival;
    private final TravelTimeDataFilter travelTimeDataFilter;

    public TravelTimeDetails(IpcArrivalDeparture departure, IpcArrivalDeparture arrival, TravelTimeDataFilter travelTimeDataFilter) {
        this.departure = departure;
        this.arrival = arrival;
        this.travelTimeDataFilter = travelTimeDataFilter;
    }


    public long getTravelTime() {
        if (this.arrival != null && this.departure != null && arrival.isArrival() && departure.isDeparture()) {
            if (sanityCheck()) {
                return this.arrival.getTime().getTime()
                        - this.getDeparture().getTime().getTime();
            } else {
                logger.warn("Outside bounds : {} ", this);
            }
        }
        return -1;
    }

    public boolean sanityCheck() {
        if (this.arrival != null && this.departure != null && arrival.isArrival() && departure.isDeparture()) {
            return !travelTimeDataFilter.filter(departure, arrival);
        }

        return false;
    }

    @Override
    public String toString() {
        return "TravelTimeDetails [departure="
                + departure
                + ", arrival="
                + arrival
                + ", getTravelTime()="
                + getTravelTime()
                + ", sanityCheck()="
                + sanityCheck()
                + "]";
    }
}
