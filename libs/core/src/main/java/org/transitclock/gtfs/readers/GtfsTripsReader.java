/* (C)2023 */
package org.transitclock.gtfs.readers;

import java.text.ParseException;

import org.transitclock.gtfs.GtfsFilter;
import org.transitclock.gtfs.model.GtfsTrip;
import org.transitclock.utils.csv.CsvBaseReader;

import org.apache.commons.csv.CSVRecord;

/**
 * GTFS reader for the trips.txt file
 *
 * @author SkiBu Smith
 */
public class GtfsTripsReader extends CsvBaseReader<GtfsTrip> {
    private final GtfsFilter filter;
    private final ReaderHelper readerHelper;
    public GtfsTripsReader(String dirName, GtfsFilter filter, ReaderHelper readerHelper) {
        super(dirName, "trips.txt", true, false);
        this.filter = filter;
        this.readerHelper = readerHelper;
    }

    @Override
    public GtfsTrip handleRecord(CSVRecord record, boolean supplemental) throws ParseException {
        if (filter.tripNotFiltered(record.get("trip_id")) && filter.routeNotFiltered(record.get("route_id"))) {
            return new GtfsTrip(record, readerHelper, supplemental, getFileName());
        }

        return null;
    }
}
