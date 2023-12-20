/* (C)2023 */
package org.transitclock.core;

/**
 * Specifies the state of the block assignment for a vehicle.
 *
 * @author SkiBu Smith
 */
public enum BlockAssignmentMethod {
    // Block assignment came from AVL feed
    AVL_FEED_BLOCK_ASSIGNMENT,

    // AVL feed provided a route assignment
    AVL_FEED_ROUTE_ASSIGNMENT,

    // Separate block feed provided the assignment. Not currently implemented!
    BLOCK_FEED,

    // The auto assignment feature provided the assignment.
    AUTO_ASSIGNER,

    // Vehicle finished the assignment or was assigned to another block
    ASSIGNMENT_TERMINATED,

    // For when another vehicle gets an exclusive assignment indicating that
    // the assignment needs to be removed from the old vehicle
    ASSIGNMENT_GRABBED,

    // Vehicle could not be matched to the assignment
    COULD_NOT_MATCH;
}
