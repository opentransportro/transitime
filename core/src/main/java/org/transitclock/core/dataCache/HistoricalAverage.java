/* (C)2023 */
package org.transitclock.core.dataCache;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Sean Og Crudden
 */
@Data
public class HistoricalAverage implements Serializable {
    private int count;
    double average;

    public HistoricalAverage() {
        count = 0;
        average = 0;
    }

    public void update(double element) {
        average = ((count * average) + element) / (count + 1);
        count = count + 1;
    }
}
