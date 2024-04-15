package org.transitclock.domain.structs;

/**
 * This enumeration is for keeping track of how the travel times were determined. This way can
 * tell of they should be overridden or not.
 */
public enum HowSet {
    // From when there are no schedule times so simply need to use a
    // default speed
    SPEED(0),

    // From interpolating data in GTFS stop_times.txt file
    SCHED(1),

    // No AVL data was available for the actual day so using data from
    // another day.
    SERVC(2),

    // No AVL data was available for the actual trip so using data from
    // a trip that is before or after the trip in question
    TRIP(3),

    // Based on actual running times as determined by AVL data
    AVL(4);

    @SuppressWarnings("unused")
    private final int value;

    HowSet(int value) {
        this.value = value;
    }

    public boolean isScheduleBased() {
        return this == SPEED || this == SCHED;
    }
}
