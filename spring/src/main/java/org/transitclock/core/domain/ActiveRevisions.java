/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * For keeping track of current revisions. This table should only have a single row, one that
 * specified the configRev and the travelTimesRev currently being used.
 *
 * @author SkiBu Smith
 */
@Data
@Document
public class ActiveRevisions {

    // Need a generated ID since Hibernate required some type
    // of ID. Both configRev and travelTimesRev
    // might get updated and with Hibernate you can't read in an
    // object, modify an ID and then write it out again. Therefore
    // configRev and travelTimesRev can't be an ID. This means
    // that need a separate ID. Yes, somewhat peculiar.
    @Id
    private Integer id;

    private int configRev;

    private int travelTimesRev;

    public ActiveRevisions() {
        configRev = -1;
        travelTimesRev = -1;
    }

    /**
     * @return True if both the configRev and travelTimesRev are both valid.
     */
    public boolean isValid() {
        return configRev >= 0 && travelTimesRev >= 0;
    }
}
