/* (C)2023 */
package org.transitclock.core.dataCache;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class HistoricalAverage implements Serializable {
    private int count;
    private double average;

    public HistoricalAverage() {
        count = 0;
        average = 0;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public void update(double element) {
        average = ((count * average) + element) / (count + 1);
        count = count + 1;
    }
}
