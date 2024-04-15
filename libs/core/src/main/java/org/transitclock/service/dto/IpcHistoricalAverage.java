/* (C)2023 */
package org.transitclock.service.dto;

import java.io.Serializable;
import org.transitclock.core.dataCache.HistoricalAverage;

/**
 * @author Sean Og Crudden Represents an historical average.
 */
public class IpcHistoricalAverage implements Serializable {

    private Integer count = 0;

    private Double average = 0.0;

    public IpcHistoricalAverage(HistoricalAverage historicalAverage) {
        if (historicalAverage != null) {
            this.count = historicalAverage.getCount();
            this.average = historicalAverage.getAverage();
        }
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }
}
