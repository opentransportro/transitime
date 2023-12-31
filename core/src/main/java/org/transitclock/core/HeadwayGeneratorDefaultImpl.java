/* (C)2023 */
package org.transitclock.core;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.db.structs.Headway;

/**
 * @author SkiBu Smith
 */
@Slf4j
public class HeadwayGeneratorDefaultImpl implements HeadwayGenerator {
    @Override
    public Headway generate(VehicleState vehicleState) {
        logger.debug("HeadwayGeneratorDefaultImpl.generate() still needs to " + "be implemented");
        return null;
    }
}
