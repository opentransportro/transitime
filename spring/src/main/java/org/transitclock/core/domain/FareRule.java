/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.transitclock.gtfs.GtfsFareRule;

import java.io.Serializable;
import java.util.Objects;

/**
 * Contains data from the fare_rules.txt GTFS file. This class is for reading/writing that data to
 * the db.
 *
 * @author SkiBu Smith
 */
@EqualsAndHashCode
@ToString
@Getter
@Document(collection = "FareRules")
public class FareRule implements Serializable {

    @Data
    private static class Key {
        private final int configRev;

        private final String fareId;

        private final String routeId;

        private final String originId;

        private final String destinationId;

        private final String containsId;
    }

    @Id
    @Delegate
    private final Key key;

    /**
     * For constructing FareRule object using GTFS data.
     *
     * @param configRev
     * @param gfr The GTFS data for the fare rule
     * @param properRouteId If the routeId should be changed to use parent route ID
     */
    public FareRule(int configRev, GtfsFareRule gfr, String properRouteId) {
        this.key = new Key(
                configRev,
                gfr.getFareId(),
                Objects.requireNonNullElseGet(properRouteId, () -> gfr.getRouteId() == null ? "" : gfr.getRouteId()),
                gfr.getOriginId() == null ? "" : gfr.getOriginId(),
                gfr.getDestinationId() == null ? "" : gfr.getDestinationId(),
                gfr.getContainsId() == null ? "" : gfr.getContainsId()
        );
    }

    public String getRouteId() {
        // With respect to the database, routeId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return key.routeId.isEmpty() ? null : key.routeId;
    }

    public String getOriginId() {
        // With respect to the database, originId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return key.originId.isEmpty() ? null : key.originId;
    }

    public String getDestinationId() {
        // With respect to the database, destinationId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return key.destinationId.isEmpty() ? null : key.destinationId;
    }

    public String getContainsId() {
        // With respect to the database, containsId cannot be null since
        // it is a primary key. But sometimes it won't be set. For this case
        // should return null instead of empty string for consistency.
        return key.containsId.isEmpty() ? null : key.containsId;
    }
}
