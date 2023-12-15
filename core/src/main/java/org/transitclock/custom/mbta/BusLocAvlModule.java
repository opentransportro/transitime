/* (C)2023 */
package org.transitclock.custom.mbta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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
 * AVL module for reading AVL data from MBTA BusLoc feed.
 *
 * @author Sean Óg Crudden
 */
public class BusLocAvlModule extends PollUrlAvlModule {

    private static StringConfigValue mbtaBusLocFeedUrl = new StringConfigValue(
            "transitclock.avl.mbtaBusLocFeedUrl",
            "https://s3.amazonaws.com/mbta-busloc-s3/VehiclePositions_enhanced.json",
            "The URL of the BusLoc json feed to use.");

    private static StringConfigValue mbtaTestRoute =
            new StringConfigValue("transitclock.avl.testroute", "77", "Route to test against.");

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
    public BusLocAvlModule(String agencyId) {
        super(agencyId);
    }

    /** Returns URL to use */
    @Override
    protected String getUrl() {
        return mbtaBusLocFeedUrl.getValue();
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

            JSONArray entities = (JSONArray) jsonObj.get("entity");

            // JSONArray vehicles = entityObj.getJSONArray("vehicle");

            for (int i = 0; i < entities.length(); i++) {
                JSONObject entity = entities.getJSONObject(i);
                JSONObject vehicle = entity.getJSONObject("vehicle");
                JSONObject vehicleIdentity = vehicle.getJSONObject("vehicle");
                JSONObject vehiclePosition = vehicle.getJSONObject("position");
                JSONObject vehicleTrip = vehicle.getJSONObject("trip");

                if (vehicleTrip.has("route_id")
                        && (mbtaTestRoute.getValue() == null
                                || vehicleTrip.getString("route_id").equals(mbtaTestRoute.getValue()))) {
                    String blockid = vehicle.getString("block_id");
                    long timestamp = vehicle.getLong("timestamp");

                    // Create the AvlReport
                    AvlReport avlReport = new AvlReport(
                            vehicleIdentity.getString("id"),
                            timestamp * 1000,
                            vehiclePosition.getDouble("latitude"),
                            vehiclePosition.getDouble("longitude"),
                            Float.NaN,
                            (float) vehiclePosition.getInt("bearing"),
                            "BusLoc");

                    // Actually set the assignment
                    avlReport.setAssignment(blockid, AssignmentType.BLOCK_ID);

                    logger.debug("From BusLocAvlModule {}", avlReport);

                    if (shouldProcessAvl) {
                        avlReportsReadIn.add(avlReport);
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
        Module.start("org.transitclock.custom.mbta.BusLocAvlModule");
    }
}
