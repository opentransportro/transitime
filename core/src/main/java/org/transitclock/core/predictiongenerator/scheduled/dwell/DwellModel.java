/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.dwell;

import java.io.Serializable;

/**
 * @author scrudden To be implemented if you want to create an algorithm to predict dwell time. The
 *     possible inputs to any such algorithms are dwelltime, headway and demand.
 */
public interface DwellModel extends Serializable {
    Integer predict(Integer headway, Integer demand);

    void putSample(Integer dwelltime, Integer headway, Integer demand);
}
