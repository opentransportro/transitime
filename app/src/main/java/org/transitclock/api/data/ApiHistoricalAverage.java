/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcHistoricalAverage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Describes an historical average
 *
 * @author Sean Og Crudden
 */
@Data
public class ApiHistoricalAverage {

    @JsonProperty
    private Integer count;

    @JsonProperty
    private Double average;

    public ApiHistoricalAverage(IpcHistoricalAverage ipcHistoricalAverage) {
        this.count = ipcHistoricalAverage.getCount();
        this.average = ipcHistoricalAverage.getAverage();
    }
}
