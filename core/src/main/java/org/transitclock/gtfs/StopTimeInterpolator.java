package org.transitclock.gtfs;

import org.transitclock.gtfs.gtfsStructs.GtfsStopTime;

import java.util.List;

public class StopTimeInterpolator {
    private final List<GtfsStopTime> stopTimes;

    public StopTimeInterpolator(List<GtfsStopTime> stopTimes) {
        this.stopTimes = stopTimes;
    }

    /**
     * Scan through the given list of stoptimes, interpolating the missing (unset) ones. This is
     * currently done by assuming equidistant stops and constant speed. While we may not be able to
     * improve the constant speed assumption, we can
     * TODO: use route matching (or shape distance etc.) to improve inter-stop distances
     */
    public void interpolate() {
        int lastStop = stopTimes.size() - 1;
        int numInterpStops;
        int departureTime = -1;
        int prevDepartureTime;
        int interpStep;

        for (int i = 0; i < lastStop; i++) {
            GtfsStopTime st0 = stopTimes.get(i);

            prevDepartureTime = departureTime;
            departureTime = st0.getDepartureTimeSecs() != null ? st0.getDepartureTimeSecs() : -1;

            // Interpolate, if necessary, the times of non-timepoint stops
            if (!(st0.isDepartureTimeSet() && st0.isArrivalTimeSet())) {
                // figure out how many such stops there are in a row.
                int j;
                GtfsStopTime st = null;
                for (j = i + 1; j < lastStop + 1; ++j) {
                    st = stopTimes.get(j);
                    if ((st.isDepartureTimeSet() && st.getDepartureTimeSecs() != departureTime) ||
                            (st.isArrivalTimeSet() && st.getArrivalTimeSecs() != departureTime)
                    ) {
                        break;
                    }
                }
                if (j == lastStop + 1) {
                    throw new RuntimeException(
                            "Could not interpolate arrival/departure time on stop " +
                                    i +
                                    " (missing final stop time) on trip " +
                                    st0.getTripId()
                    );
                }
                numInterpStops = j - i;
                int arrivalTime;
                if (st.isArrivalTimeSet()) {
                    arrivalTime = st.getArrivalTimeSecs();
                } else {
                    arrivalTime = st.getDepartureTimeSecs();
                }
                interpStep = (arrivalTime - prevDepartureTime) / (numInterpStops + 1);
                if (interpStep < 0) {
                    throw new RuntimeException("trip goes backwards for some reason");
                }
                for (j = i; j < i + numInterpStops; ++j) {
                    //System.out.println("interpolating " + j + " between " + prevDepartureTime + " and " + arrivalTime);
                    departureTime = prevDepartureTime + interpStep * (j - i + 1);
                    st = stopTimes.get(j);
                    if (st.isArrivalTimeSet()) {
                        departureTime = st.getArrivalTimeSecs();
                    } else {
                        st.setArrivalTimeSecs(departureTime);
                    }
                    if (!st.isDepartureTimeSet()) {
                        st.setDepartureTimeSecs(departureTime);
                    }
                }
                i = j - 1;
            }
        }
    }
}
