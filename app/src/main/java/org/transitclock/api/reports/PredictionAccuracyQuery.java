/* (C)2023 */
package org.transitclock.api.reports;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.domain.GenericQuery;
import org.transitclock.utils.Time;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For doing SQL query and generating JSON data for a prediction accuracy chart. This abstract class
 * does the SQL query and puts data into a map. Then a subclass must be used to convert the data to
 * JSON rows and columns for Google chart.
 *
 * <p>TODO: rewrite as hibernate criteria.
 *
 * @author SkiBu Smith
 */
@Slf4j
public abstract class PredictionAccuracyQuery extends GenericQuery {
    protected static final int MAX_PRED_LENGTH = 900;
    protected static final int PREDICTION_LENGTH_BUCKET_SIZE = 30;

    // Keyed on source (so can show data for multiple sources at
    // once in order to compare prediction accuracy. Contains a array,
    // with an element for each prediction bucket, containing an array
    // of the prediction accuracy values in seconds for that bucket. Each bucket
    // is for
    // a certain prediction range, specified by predictionLengthBucketSize.
    protected final Map<String, List<List<Integer>>> map = new HashMap<>();

    // Defines the output type for the intervals, whether should show
    // standard deviation, percentage, or both.
    // Can iterate over the enumerated type using:
    // for (IntervalsType type : IntervalsType.values()) {}
    public enum IntervalsType {
        PERCENTAGE("PERCENTAGE"),
        STD_DEV("STD_DEV"),
        BOTH("BOTH");

        private final String text;

        IntervalsType(final String text) {
            this.text = text;
        }

        /**
         * For converting from a string to an IntervalsType
         *
         * @param text String to be converted
         * @return The corresponding IntervalsType, or IntervalsType.PERCENTAGE as the default if
         *     text doesn't match a type.
         */
        public static IntervalsType createIntervalsType(String text) {
            for (IntervalsType type : IntervalsType.values()) {
                if (type.toString().equals(text)) {
                    return type;
                }
            }

            // If a bad non-null value was specified then log the error
            if (text != null) logger.error("\"{}\" is not a valid IntervalsType", text);

            // Couldn't match so use default value
            return IntervalsType.PERCENTAGE;
        }

        @Override
        public String toString() {
            return text;
        }
    }


    public PredictionAccuracyQuery(String agencyId) throws SQLException {
        super(agencyId);
    }

    /**
     * Determines which prediction bucket in the map to use. Want to have each bucket to be for an
     * easily understood value, such as 1 minute. Best way to do this is then have the predictions
     * for that bucket be 45 seconds to 75 seconds so that the indicator for the bucket (1 minute)
     * is in the middle of the range.
     *
     * @param predLength
     * @return
     */
    private static int index(int predLength) {
        return (predLength + PREDICTION_LENGTH_BUCKET_SIZE / 2) / PREDICTION_LENGTH_BUCKET_SIZE;
    }

    /**
     * Puts the data from the query into the map so it can be further processed later.
     *
     * @param predLength
     * @param predAccuracy
     * @param source
     */
    private void addDataToMap(int predLength, int predAccuracy, String source) {
        // Get the prediction buckets for the specified source
        List<List<Integer>> predictionBuckets = map.computeIfAbsent(source, k -> new ArrayList<>());

        // Determine the index of the appropriate prediction bucket
        int predictionBucketIndex = index(predLength);

        while (predictionBuckets.size() < predictionBucketIndex + 1) predictionBuckets.add(new ArrayList<>());
        if (predictionBucketIndex < predictionBuckets.size() && predictionBucketIndex >= 0) {
            List<Integer> predictionAccuracies = predictionBuckets.get(predictionBucketIndex);
            // Add the prediction accuracy to the bucket.
            predictionAccuracies.add(predAccuracy);
        } else {
            // some prediction streams supply predictions in the past -- ignore those
            logger.error(
                    "predictionLength {} has illegal index {} for predAccuracy {} and source {}",
                    predLength,
                    predictionBucketIndex,
                    predAccuracy,
                    source);
        }
    }

    /**
     * Performs the SQL query and puts the resulting data into the map.
     *
     * @param beginDateStr Begin date for date range of data to use.
     * @param numDaysStr How many days to do the query for
     * @param beginTimeStr For specifying time of day between the begin and end date to use data
     *     for. Can thereby specify a date range of a week but then just look at data for particular
     *     time of day, such as 7am to 9am, for those days. Set to null or empty string to use data
     *     for entire day.
     * @param endTimeStr For specifying time of day between the begin and end date to use data for.
     *     Can thereby specify a date range of a week but then just look at data for particular time
     *     of day, such as 7am to 9am, for those days. Set to null or empty string to use data for
     *     entire day.
     * @param routeIds Array of IDs of routes to get data for
     * @param predSource The source of the predictions. Can be null or "" (for all), "Transitime",
     *     or "Other"
     * @param predType Whether predictions are affected by wait stop. Can be "" (for all),
     *     "AffectedByWaitStop", or "NotAffectedByWaitStop".
     * @throws SQLException
     * @throws ParseException
     */
    protected void doQuery(
            String beginDateStr,
            String numDaysStr,
            String beginTimeStr,
            String endTimeStr,
            String[] routeIds,
            String predSource,
            String predType)
            throws SQLException, ParseException {
        // Make sure not trying to get data for too long of a time span since
        // that could bog down the database.
        int numDays = Integer.parseInt(numDaysStr);
        if (numDays > 31) {
            throw new ParseException(
                    "Begin date to end date spans more than a month for endDate="
                            + " startDate="
                            + Time.parseDate(beginDateStr)
                            + " Number of days of "
                            + numDays
                            + " spans more than a month",
                    0);
        }
        String timeSql = "";
        String mySqlTimeSql = "";
        if ((beginTimeStr != null && !beginTimeStr.isEmpty()) || (endTimeStr != null && !endTimeStr.isEmpty())) {
            // If only begin or only end time set then use default value
            if (beginTimeStr == null || beginTimeStr.isEmpty()) beginTimeStr = "00:00:00";
            else {
                // beginTimeStr set so make sure it is valid, and prevent
                // possible SQL injection
                if (!beginTimeStr.matches("\\d+:\\d+"))
                    throw new ParseException("begin time \"" + beginTimeStr + "\" is not valid.", 0);
            }
            if (endTimeStr == null || endTimeStr.isEmpty()) endTimeStr = "23:59:59";
            // time param is jdbc param -- no need to check for injection attacks
            timeSql = " AND arrival_departure_time::time BETWEEN ? AND ? ";
        }

        // Determine route portion of SQL
        // Need to examine each route ID twice since doing a
        // routeId='stableId' OR routeShortName='stableId' in
        // order to handle agencies where GTFS route_id is not
        // stable but the GTFS route_short_name is.
        String routeSql = "";
        if (routeIds != null && routeIds.length > 0 && !routeIds[0].trim().isEmpty()) {
            routeSql = " AND (route_id=? OR route_short_name=?";
            for (int i = 1; i < routeIds.length; ++i) {
                routeSql += " OR route_id=? OR route_short_name=?";
            }
            routeSql += ")";
        }

        // Determine the source portion of the SQL. Default is to provide
        // predictions for all sources
        String sourceSql = "";
        if (predSource != null && !predSource.isEmpty()) {
            if (predSource.equals("Transitime")) {
                // Only "Transitime" predictions
                sourceSql = " AND prediction_source='Transitime'";
            } else {
                // Anything but "Transitime"
                sourceSql = " AND prediction_source<>'Transitime'";
            }
        }

        // Determine SQL for prediction type. Can be "" (for
        // all), "AffectedByWaitStop", or "NotAffectedByWaitStop".
        String predTypeSql = "";
        if (predType != null && !predType.isEmpty()) {
            if (predSource.equals("AffectedByWaitStop")) {
                // Only "AffectedByLayover" predictions
                predTypeSql = " AND affected_by_wait_stop = true ";
            } else {
                // Only "NotAffectedByLayover" predictions
                predTypeSql = " AND affected_by_wait_stop = false ";
            }
        }
        // TODO generate database independent SQL if possible!
        // Put the entire SQL query together
        String sql = "SELECT to_char(predicted_time-prediction_read_time, 'SSSS')::integer as predLength, "
                + "prediction_accuracy_msecs/1000 as predAccuracy, "
                + " prediction_source as source  FROM prediction_accuracy WHERE"
                + " arrival_departure_time BETWEEN ? AND TIMESTAMP '" + beginDateStr
                + "' + INTERVAL '"
                + numDays
                + " day' "
                + timeSql
                + "  AND predicted_time - prediction_read_time < '00:15:00' "
                + routeSql
                + sourceSql
                + predTypeSql;



        PreparedStatement statement = null;
        try {
            logger.debug("SQL: {}", sql);
            statement = getConnection().prepareStatement(sql);

            // Determine the date parameters for the query
            Timestamp beginDate = null;
            java.util.Date date = Time.parse(beginDateStr);
            beginDate = new Timestamp(date.getTime());

            // Determine the time parameters for the query
            // If begin time not set but end time is then use midnight as begin
            // time
            if ((beginTimeStr == null || beginTimeStr.isEmpty()) && endTimeStr != null && !endTimeStr.isEmpty()) {
                beginTimeStr = "00:00:00";
            }
            // If end time not set but begin time is then use midnight as end
            // time
            if ((endTimeStr == null || endTimeStr.isEmpty()) && beginTimeStr != null && !beginTimeStr.isEmpty()) {
                endTimeStr = "23:59:59";
            }

            java.sql.Time beginTime = null;
            java.sql.Time endTime = null;
            if (beginTimeStr != null && !beginTimeStr.isEmpty()) {
                beginTime = new java.sql.Time(Time.parseTimeOfDay(beginTimeStr) * Time.MS_PER_SEC);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = new java.sql.Time(Time.parseTimeOfDay(endTimeStr) * Time.MS_PER_SEC);
            }

            logger.debug(
                    "beginDate {} beginDateStr {} endDateStr {} beginTime {} beginTimeStr {}"
                            + " endTime {} endTimeStr {}",
                    beginDate,
                    beginDateStr,
                    numDays,
                    beginTime,
                    beginTimeStr,
                    endTime,
                    endTimeStr);

            // Set the parameters for the query
            int i = 1;
            statement.setTimestamp(i++, beginDate);

            if (beginTime != null) {
                statement.setTime(i++, beginTime);
            }
            if (endTime != null) {
                statement.setTime(i++, endTime);
            }
            if (routeIds != null) {
                for (String routeId : routeIds)
                    if (!routeId.trim().isEmpty()) {
                        // Need to add the route ID twice since doing a
                        // routeId='stableId' OR routeShortName='stableId' in
                        // order to handle agencies where GTFS route_id is not
                        // stable but the GTFS route_short_name is.
                        statement.setString(i++, routeId);
                        statement.setString(i++, routeId);
                    }
            }

            // Actually execute the query
            ResultSet rs = statement.executeQuery();

            // Process results of query
            while (rs.next()) {
                int predLength = rs.getInt("predLength");
                int predAccuracy = rs.getInt("predAccuracy");
                String sourceResult = rs.getString("source");

                addDataToMap(predLength, predAccuracy, sourceResult);
                logger.debug("predLength={} predAccuracy={} source={}", predLength, predAccuracy, sourceResult);
            }

            rs.close();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null)
                statement.close();
            if (!getConnection().isClosed()) {
                getConnection().close();
            }
        }
    }
}
