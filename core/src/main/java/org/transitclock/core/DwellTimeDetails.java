/* (C)2023 */
package org.transitclock.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.transitclock.configData.CoreConfig;
import org.transitclock.service.dto.IpcArrivalDeparture;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DwellTimeDetails {
    private final IpcArrivalDeparture arrival;
    private final IpcArrivalDeparture departure;

    public long getDwellTime() {
        if (this.arrival != null && this.departure != null && arrival.isArrival() && departure.isDeparture()) {

            if (sanityCheck()) {
                return this.departure.getTime().getTime() - this.arrival.getTime().getTime();
            } else {
                logger.warn("Outside bounds : {} ", this);
            }
        }
        return -1;
    }

    public boolean sanityCheck() {
        if (this.arrival != null && this.departure != null && arrival.isArrival() && departure.isDeparture()) {
            long dwellTime = this.departure.getTime().getTime() - this.arrival.getTime().getTime();
            return dwellTime >= 0 && dwellTime <= CoreConfig.maxDwellTime.getValue();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "DwellTimeDetails [departure="
                + departure
                + ", arrival="
                + arrival
                + ", getDwellTime()="
                + getDwellTime()
                + ", sanityCheck()="
                + sanityCheck()
                + "]";
    }
}
