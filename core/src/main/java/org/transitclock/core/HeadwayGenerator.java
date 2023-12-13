/* (C)2023 */
package org.transitclock.core;

import org.transitclock.db.structs.Headway;

/**
 * Defines the interface for generating headway information. To create headway info using an
 * alternate method simply implement this interface and configure HeadwayGeneratorFactory to
 * instantiate the new class when a HeadwayGenerator is needed.
 *
 * @author SkiBu Smith
 */
public interface HeadwayGenerator {
    /**
     * Generates headway info. This interface likely will need to be changed in the future to return
     * the headways generated such that the MatchProcessor can manage them and store them away.
     *
     * @param vehicleState
     */
    public Headway generate(VehicleState vehicleState);
}
