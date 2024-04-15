package org.transitclock.domain.structs;

// The type of the assignment received in the AVL feed
public enum AssignmentType {
    UNSET,
    BLOCK_ID,
    // For when creating schedule based predictions
    BLOCK_FOR_SCHED_BASED_PREDS,
    ROUTE_ID,
    TRIP_ID,
    TRIP_SHORT_NAME,
    // For when get bad assignment info from AVL feed
    PREVIOUS
}
