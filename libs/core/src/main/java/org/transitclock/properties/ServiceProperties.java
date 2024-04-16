package org.transitclock.properties;

import lombok.Data;

@Data
public class ServiceProperties {
    // config param: transitclock.service.minutesIntoMorningToIncludePreviousServiceIds
    // Early in the morning also want to include at service IDs for previous day since a block might have started on that day. But don't want to always include previous day service IDs since that confuses things. Therefore just include them if before this time of the day, in minutes.
    private Integer minutesIntoMorningToIncludePreviousServiceIds = 240;

}
