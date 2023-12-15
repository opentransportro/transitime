/* (C)2023 */
package org.transitclock.custom.mbta;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.avl.AvlModule;
import org.transitclock.avl.PollUrlAvlModule;
import org.transitclock.config.StringConfigValue;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.db.structs.AvlReport.AssignmentType;
import org.transitclock.Module;

/**
 * AVL module for reading AVL data from MBTA High freq feed.
 *
 * @author Sean Óg Crudden
 */
public class HighFreqAvlModule extends PollUrlAvlModule {

    private static StringConfigValue feedUrl = new StringConfigValue(
            "transitclock.avl.feedUrl",
            "https://api-v3.mbta.com/vehicles/?filter%5Bid%5D=y1696,y1708,y1709,y1710,y1717,y1718&sort=-updated_at",
            "The URL of the BusLoc json feed to use.");

    private static StringConfigValue mbtaTestRoute =
            new StringConfigValue("transitclock.avl.testroute", null, "Route to test against.");

    // If debugging feed and want to not actually process
    // AVL reports to generate predictions and such then
    // set shouldProcessAvl to false;
    private static boolean shouldProcessAvl = true;

    // For logging use AvlModule class so that will end up in the AVL log file
    private static final Logger logger = LoggerFactory.getLogger(AvlModule.class);

    /********************** Member Functions **************************/

    /**
     * Constructor
     *
     * @param agencyId
     */
    public HighFreqAvlModule(String agencyId) {
        super(agencyId);
    }

    /** Returns URL to use */
    @Override
    protected String getUrl() {
        return feedUrl.getValue();
    }

    /**
     * Called when AVL data is read from URL. Processes the JSON data and calls processAvlReport()
     * for each AVL report.
     */
    @Override
    protected Collection<AvlReport> processData(InputStream in) throws Exception {
        // Get the JSON string containing the AVL data
        String jsonStr = getJsonString(in);
        Collection<AvlReport> avlReportsReadIn = new ArrayList<AvlReport>();
        try {
            // Convert JSON string to a JSON object
            JSONObject jsonObj = new JSONObject(jsonStr);

            JSONArray entities = (JSONArray) jsonObj.get("data");

            // JSONArray vehicles = entityObj.getJSONArray("vehicle");

            for (int i = 0; i < entities.length(); i++) {
                JSONObject entity = entities.getJSONObject(i);

                JSONObject relationships = entity.getJSONObject("relationships");
                JSONObject attributes = entity.getJSONObject("attributes");
                JSONObject trip = relationships.getJSONObject("trip");
                JSONObject tripData = trip.getJSONObject("data");
                JSONObject route = relationships.getJSONObject("route");
                JSONObject routeData = route.getJSONObject("data");

                if (routeData.has("id")) {
                    if (mbtaTestRoute.getValue() == null
                            || routeData.getString("id").equals(mbtaTestRoute.getValue())) {
                        String tripid = tripData.getString("id");
                        String timeStr = attributes.getString("updated_at");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                        Date time = dateFormat.parse(timeStr);

                        AvlReport avlReport = new AvlReport(
                                entity.getString("id"),
                                time.getTime(),
                                attributes.getDouble("latitude"),
                                attributes.getDouble("longitude"),
                                Float.NaN,
                                (float) attributes.getDouble("bearing"),
                                "HighFreq");

                        avlReport.setAssignment(tripid, AssignmentType.TRIP_ID);

                        logger.debug("From HighFreqAvlModule {}", avlReport);

                        if (shouldProcessAvl) {
                            avlReportsReadIn.add(avlReport);
                        }
                    }
                }
            }
            // Return all the AVL reports read in
            return avlReportsReadIn;
        } catch (JSONException e) {
            logger.error("Error parsing JSON. {}. {}", e.getMessage(), jsonStr, e);
            return new ArrayList<AvlReport>();
        }
    }

    /** Just for debugging */
    public static void main(String[] args) {
        // For debugging turn off the actual processing of the AVL data.
        // This way the AVL data is logged, but that is all.
        shouldProcessAvl = false;

        // Create a BusLocAvlModule for testing
        Module.start("org.transitclock.custom.mbta.HighFreqAvlModule");
    }
}
