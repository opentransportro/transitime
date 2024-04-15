/* (C)2023 */
package org.transitclock.core.holdingmethod;

import java.util.Comparator;
import org.transitclock.service.dto.IpcPrediction;

/**
 * @author Sean Óg Crudden Compare the time of two predictions. Used to put predictions in order.
 */
public class PredictionTimeComparator implements Comparator<IpcPrediction> {
    @Override
    public int compare(IpcPrediction o1, IpcPrediction o2) {
        return Long.compare(o1.getPredictionTime(), o2.getPredictionTime());
    }
}
