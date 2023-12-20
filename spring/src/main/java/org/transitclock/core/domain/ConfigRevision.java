/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * For keeping track of information having to do with a configuration revision. This way can keep
 * track of reason for processing config, when it was run, etc.
 *
 * @author Michael Smith (michael@transitclock.org)
 */
@Data
@Document
public class ConfigRevision {

    @Id
    private final int configRev;

    private final Date processedTime;

    private final Date zipFileLastModifiedTime;

    private final String notes;
}
