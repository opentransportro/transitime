/* (C)2023 */
package org.transitclock.core.predAccuracy.gtfsrt;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import java.util.Date;

public interface GTFSRealtimeTranslator {
    String parseStopId(String inputStopId);

    Date parseFeedHeaderTimestamp(FeedHeader header);
}
