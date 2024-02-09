/* (C)2023 */
package org.transitclock.api.reports;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.api.reports.ChartJsonBuilder.RowBuilder;
import org.transitclock.utils.StringUtils;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * For generating the JSON data for a Google chart for showing percent of predictions that lie
 * between an error range.
 *
 * @author SkiBu Smith
 */
@Slf4j
public class PredAccuracyRangeQuery extends PredictionAccuracyQuery {
    private final ResourceBundle labels;


    /**
     * Creates connection to database specified by the agencyId.
     *
     * @param agencyId
     * @throws SQLException
     */
    public PredAccuracyRangeQuery(String agencyId) throws SQLException {
        super(agencyId);
        labels = ResourceBundle.getBundle("org.transitclock.i18n.text", Locale.getDefault());
    }

    /**
     * Adds the column definition in JSON string format so that chart the data using Google charts.
     * The column definition describes the contents of each column but doesn't actually contain the
     * data itself.
     *
     * @param builder
     * @param maxEarlySec
     * @param maxLateSec
     */
    private void addCols(ChartJsonBuilder builder, int maxEarlySec, int maxLateSec) {
        if (map.isEmpty()) {
            logger.error("Called PredAccuracyStackQuery.addCols() but there " + "is no data in the map.");
            return;
        }

        builder.addNumberColumn();
        builder.addNumberColumn(labels.getString("EarlierThanPredicted_MoreThan")
                + " "
                + maxEarlySec
                + " "
                + labels.getString("SecsEarly_"));
        builder.addTooltipColumn();
        builder.addNumberColumn(labels.getString("WithinBounds_")
                + " "
                + maxEarlySec
                + " "
                + labels.getString("SecsEarlyTo")
                + " "
                + maxLateSec
                + " "
                + labels.getString("SecsLate_"));
        builder.addTooltipColumn();
        builder.addNumberColumn(
                labels.getString("LaterThanPredicted_MoreThan") + maxLateSec + " " + labels.getString("SecsLate_"));
        builder.addTooltipColumn();
    }

    /**
     * Adds the row definition in JSON string format so that chart the data using Google charts. The
     * row definition contains the actual data.
     *
     * @param builder
     * @param maxEarlySec
     * @param maxLateSec
     */
    private void addRows(ChartJsonBuilder builder, int maxEarlySec, int maxLateSec) {
        if (map.isEmpty()) {
            logger.error("Called PredAccuracyStackQuery.getCols() but there " + "is no data in the map.");
            return;
        }

        // Only dealing with a single source so get data for that source
        List<List<Integer>> dataForSource = null;
        for (String source : map.keySet()) {
            dataForSource = map.get(source);
        }

        // For each prediction length bucket...
        for (int predBucketIdx = 0; predBucketIdx <= MAX_PRED_LENGTH / PREDICTION_LENGTH_BUCKET_SIZE; ++predBucketIdx) {
            // Prediction length in seconds
            double predBucketSecs = predBucketIdx * PREDICTION_LENGTH_BUCKET_SIZE / 60.0;

            List<Integer> listForPredBucket = null;
            if (dataForSource != null && dataForSource.size() > predBucketIdx) {
                listForPredBucket = dataForSource.get(predBucketIdx);

                // For this prediction bucket track whether prediction below
                // min,
                // between min and max, and above max.
                int tooEarly = 0, ok = 0, tooLate = 0;
                for (int accuracyInSecs : listForPredBucket) {
                    if (accuracyInSecs < -maxEarlySec) ++tooEarly;
                    else if (accuracyInSecs < maxLateSec) ++ok;
                    else ++tooLate;
                }

                // If no data for this prediction bucket then continue to next
                // one
                int numPreds = listForPredBucket.size();
                if (numPreds == 0) continue;

                double tooEarlyPercentage = 100.0 * tooEarly / numPreds;
                double okPercentage = 100.0 * ok / numPreds;
                double tooLatePercentage = 100.0 * tooLate / numPreds;

                RowBuilder rowBuilder = builder.newRow();
                rowBuilder.addRowElement(predBucketSecs);

                rowBuilder.addRowElement(tooEarlyPercentage);

                rowBuilder.addRowElement(labels.getString("EarlierThanPredicted")
                        + ": "
                        + tooEarly
                        + " "
                        + labels.getString("Points")
                        + ", "
                        + StringUtils.oneDigitFormat(tooEarlyPercentage)
                        + "%");

                rowBuilder.addRowElement(okPercentage);
                rowBuilder.addRowElement(labels.getString("WithinBounds")
                        + ": "
                        + ok
                        + " "
                        + labels.getString("Points")
                        + ", "
                        + StringUtils.oneDigitFormat(okPercentage)
                        + "%");

                rowBuilder.addRowElement(tooLatePercentage);
                rowBuilder.addRowElement(labels.getString("TooLate")
                        + ": "
                        + tooLate
                        + " "
                        + labels.getString("Points")
                        + ", "
                        + StringUtils.oneDigitFormat(tooLatePercentage)
                        + "%");
                rowBuilder.addRowElement(labels.getString("LaterThanPredicted")
                        + ": "
                        + tooLate
                        + " "
                        + labels.getString("Points")
                        + ", "
                        + StringUtils.oneDigitFormat(tooLatePercentage)
                        + "%");
            }
        }
    }

    /**
     * Performs the query and returns the data in an JSON string so that it can be used for a chart.
     *
     * @param beginDateStr Begin date for date range of data to use.
     * @param endDateStr End date for date range of data to use. Since want to include data for the
     *     end date, 1 day is added to the end date for the query.
     * @param beginTimeStr For specifying time of day between the begin and end date to use data
     *     for. Can thereby specify a date range of a week but then just look at data for particular
     *     time of day, such as 7am to 9am, for those days. Set to null or empty string to use data
     *     for entire day.
     * @param numDays How long query should be run for.
     * @param routeIds Specifies which routes to do the query for. Can be null for all routes or an
     *     array of route IDs.
     * @param predSource The source of the predictions. Can be null or "" (for all), "Transitime",
     *     or "Other"
     * @param predType Whether predictions are affected by wait stop. Can be "" (for all),
     *     "AffectedByWaitStop", or "NotAffectedByWaitStop".
     * @param maxEarlySec How early in msec a prediction is allowed to be. Should be a positive
     *     value.
     * @param maxLateSec How late a in msec a prediction is allowed to be. Should be a positive
     *     value.
     * @return the full JSON string contain both cols and rows info, or null if no data returned
     *     from query
     * @throws SQLException
     * @throws ParseException
     */
    public String getJson(
            String beginDateStr,
            String numDays,
            String beginTimeStr,
            String endTimeStr,
            String[] routeIds,
            String predSource,
            String predType,
            int maxEarlySec,
            int maxLateSec)
            throws SQLException, ParseException {
        // Actually perform the query
        doQuery(beginDateStr, numDays, beginTimeStr, endTimeStr, routeIds, predSource, predType);

        // If query returned no data then simply return null so that
        // can easily see that there is a problem
        if (map.isEmpty()) {
            return null;
        }

        ChartJsonBuilder builder = new ChartJsonBuilder();
        addCols(builder, maxEarlySec, maxLateSec);
        addRows(builder, maxEarlySec, maxLateSec);

        return builder.getJson();
    }
}
