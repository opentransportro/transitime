package org.transitclock.properties;

import lombok.Data;

@Data
public class UpdatesProperties {
    // config param: transitclock.updates.pageDbReads
    // page database reads to break up long reads. It may impact performance on MySql
    private Boolean pageDbReads = true;

    // config param: transitclock.updates.pageSize
    // Number of records to read in at a time
    private Integer pageSize = 50000;

}
