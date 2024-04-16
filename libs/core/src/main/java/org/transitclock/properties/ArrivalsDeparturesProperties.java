package org.transitclock.properties;

import lombok.Data;

@Data
public class ArrivalsDeparturesProperties {
    // config param: transitclock.arrivalsDepartures.maxStopsWhenNoPreviousMatch
    // If vehicle just became predictable as indicated by no previous match then still want to determine arrival/departure times for earlier stops so that won't miss recording data for them them. But only want to go so far. Otherwise could be generating fake arrival/departure times when vehicle did not actually traverse that stop.
    private Integer maxStopsWhenNoPreviousMatch = 1;

    // config param: transitclock.arrivalsDepartures.maxStopsBetweenMatches
    // If between AVL reports the vehicle appears to traverse many stops then something is likely wrong with the matching. So this parameter is used to limit how many arrivals/departures are created between AVL reports.
    private Integer maxStopsBetweenMatches = 12;

    // config param: transitclock.arrivalsDepartures.allowableDifferenceBetweenAvlTimeSecs
    // If the time of a determine arrival/departure is really different from the AVL time then something must be wrong and the situation will be logged.
    private Integer allowableDifferenceBetweenAvlTimeSecs = 86400;

}
