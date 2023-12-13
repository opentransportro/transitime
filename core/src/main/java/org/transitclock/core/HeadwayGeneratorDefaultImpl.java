/* (C)2023 */
package org.transitclock.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.db.structs.Headway;

/**
 * @author SkiBu Smith
 */
public class HeadwayGeneratorDefaultImpl implements HeadwayGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HeadwayGeneratorDefaultImpl.class);

    /********************** Member Functions
     * @return **************************/

    /* (non-Javadoc)
     * @see org.transitclock.core.HeadwayGenerator#generate(org.transitclock.core.VehicleState)
     */
    @Override
    public Headway generate(VehicleState vehicleState) {
        // FIXME Still needs to be implemented!!
        logger.debug("HeadwayGeneratorDefaultImpl.generate() still needs to " + "be implemented");
        return null;
    }
}
