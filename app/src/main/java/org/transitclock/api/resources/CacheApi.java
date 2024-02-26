/* (C)2023 */
package org.transitclock.api.resources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.transitclock.api.data.*;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.service.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Contains the API commands for the Transitime API for getting info on data that is cached.
 *
 * <p>The data output can be in either JSON or XML. The output format is specified by the accept
 * header or by using the query string parameter "format=json" or "format=xml".
 *
 * @author SkiBu Smith
 */
@RestController
@RequestMapping("/key/{key}/agency/{agency}")
public class CacheApi extends BaseApiResource {

    @GetMapping(
        value = "/command/kalmanerrorcachekeys",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Gets the list of Kalman Cache error.",
            description = "Gets the list of Kalman Cache error.",
            tags = {"kalman", "cache"})
    public ResponseEntity<ApiKalmanErrorCacheKeys> getKalmanErrorCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcKalmanErrorCacheKey> result = cacheQueryInterface.getKalmanErrorCacheKeys();
            ApiKalmanErrorCacheKeys keys = new ApiKalmanErrorCacheKeys(result);
            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/scheduledbasedhistoricalaveragecachekeys",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Gets a list of the keys that have values in the historical average cache for"
                    + " schedules based services.",
            description = "Gets a list of the keys that have values in the historical average cache for"
                    + " schedules based services.",
            tags = {"cache"})
    public ResponseEntity<ApiHistoricalAverageCacheKeys> getSchedulesBasedHistoricalAverageCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHistoricalAverageCacheKey> result =
                    cacheQueryInterface.getScheduledBasedHistoricalAverageCacheKeys();

            ApiHistoricalAverageCacheKeys keys = new ApiHistoricalAverageCacheKeys(result);

            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    // TODO This is not completed and should not be used.
    @GetMapping(
        value = "/command/frequencybasedhistoricalaveragecachekeys",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Gets a list of the keys that have values in the historical average cache for"
                    + " frequency based services.",
            description = "Gets a list of the keys that have values in the historical average cache for"
                    + " frequency based services.<font color=\"#FF0000\">This is not completed"
                    + " and should not be used.<font>",
            tags = {"cache"})
    public ResponseEntity<ApiHistoricalAverageCacheKeys> getFrequencyBasedHistoricalAverageCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHistoricalAverageCacheKey> result =
                    cacheQueryInterface.getFrequencyBasedHistoricalAverageCacheKeys();

            ApiHistoricalAverageCacheKeys keys = new ApiHistoricalAverageCacheKeys(result);

            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/holdingtimecachekeys",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Gets a list of the keys for the holding times in the cache.",
            description = "Gets a list of the keys for the holding times in the cache.",
            tags = {"cache"})
    public ResponseEntity<ApiHoldingTimeCacheKeys> getHoldingTimeCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHoldingTimeCacheKey> result = cacheQueryInterface.getHoldingTimeCacheKeys();

            ApiHoldingTimeCacheKeys keys = new ApiHoldingTimeCacheKeys(result);

            var response = stdParameters.createResponse(keys);

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    /**
     * Returns info about a cache.
     *
     * @param stdParameters
     * @param cachename this is the name of the cache to get the size of.
     * @return
     * @
     */
    @GetMapping(
        value = "/command/cacheinfo",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns the number of entries in the cacheName cache.",
            description = "Returns the number of entries in the cacheName cache. The name is passed"
                    + " throug the cachename parameter.",
            tags = {"cache"})
    public ResponseEntity<ApiCacheDetails> getCacheInfo(
            StandardParameters stdParameters,
            @Parameter(description = "Name of the cache", required = true) @RequestParam(value = "cachename")
                    String cachename)
             {
        try {

            Integer size = cacheQueryInterface.entriesInCache(cachename);

            if (size != null)
                return stdParameters.createResponse(new ApiCacheDetails(cachename, size));
            else
                throw new Exception("No cache named:" + cachename);

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/stoparrivaldeparturecachedata",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns a list of current arrival or departure events for a specified stop"
                    + " that are in the cache.",
            description = "Returns a list of current arrival or departure events for a specified stop"
                    + " that are in the cache.",
            tags = {"cache"})
    public ResponseEntity<ApiArrivalDepartures> getStopArrivalDepartureCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "Stop Id.", required = true)
            @RequestParam(value = "stopid") String stopid,
            @RequestParam(value = "date") Date date)
             {
        try {

            List<IpcArrivalDeparture> result = cacheQueryInterface.getStopArrivalDepartures(stopid);

            ApiArrivalDepartures apiResult = new ApiArrivalDepartures(result);
            var response = stdParameters.createResponse(apiResult);
            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/triparrivaldeparturecachedata",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns the arrivals and departures for a trip on a specific day and start" + " time.",
            description = "Returns a list  of the arrivals and departures for a trip on a specific day"
                    + " and start time.Either tripId or date must be specified.",
            tags = {"cache"})
    public ResponseEntity<ApiArrivalDepartures> getTripArrivalDepartureCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "if specified, returns the list for that tripId.", required = false)
                    @RequestParam(value = "tripId")
                    String tripid,
            @Parameter(description = "if specified, returns the list for that date.", required = false)
                    @RequestParam(value = "date")
                    DateParam date,
            @Parameter(description = "if specified, returns the list for that starttime.", required = false)
                    @RequestParam(value = "starttime")
                    Integer starttime)
             {
        try {

            LocalDate queryDate = null;
            if (date != null) queryDate = date.getDate();
            List<IpcArrivalDeparture> result =
                    cacheQueryInterface.getTripArrivalDepartures(tripid, queryDate, starttime);

            ApiArrivalDepartures apiResult = new ApiArrivalDepartures(result);
            var response = stdParameters.createResponse(apiResult);
            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    /*
     * This will give the historical cache value for an individual stop path
     * index of a trip private String tripId; private Integer stopPathIndex;
     */
    @GetMapping(
        value = "/command/historicalaveragecachedata",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns the historical cache value for an individual stop path index of a" + " trip.",
            description = "Returns the historical cache value for an individual stop path index of a" + " trip.",
            tags = {"cache"})
    public ResponseEntity<ApiHistoricalAverage> getHistoricalAverageCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
                    Integer stopPathIndex) {
        try {
            IpcHistoricalAverage result = cacheQueryInterface.getHistoricalAverage(tripId, stopPathIndex);
            var response = stdParameters.createResponse(new ApiHistoricalAverage(result));
            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/getkalmanerrorvalue",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns the latest Kalman error value for a the stop path of a trip.",
            description = "Returns the latest Kalman error value for a the stop path of a trip.",
            tags = {"kalman", "cache"})
    public ResponseEntity<Double> getKalmanErrorValue(
            StandardParameters stdParameters,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
                    Integer stopPathIndex) {
        try {

            Double result = cacheQueryInterface.getKalmanErrorValue(tripId, stopPathIndex);

            var response = stdParameters.createResponse(result);

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/getstoppathpredictions",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns a list of predictions for a the stop path of a trip.",
            description = "Returns a list of predictions for a the stop path of a trip.",
            tags = {"cache"})
    // TODO: (vsperez) I believe date is not used at all
    public ResponseEntity<ApiPredictionsForStopPath> getStopPathPredictions(
            StandardParameters stdParameters,
            @Parameter(description = "Algorith used for calculating the perdiction", required = false)
                    @RequestParam(value = "algorithm")
                    String algorithm,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
                    Integer stopPathIndex,
            @Parameter(description = "Specified the date.", required = true) @RequestParam(value = "date")
                    DateParam date) {
        try {
            LocalTime midnight = LocalTime.MIDNIGHT;
            Date end_date = null;
            Date start_date = null;
            if (date != null) {
                LocalDate now = date.getDate();

                LocalDateTime todayMidnight = LocalDateTime.of(now, midnight);
                LocalDateTime yesterdatMidnight = todayMidnight.plusDays(-1);

                end_date =
                        Date.from(todayMidnight.atZone(ZoneId.systemDefault()).toInstant());
                start_date = Date.from(
                        yesterdatMidnight.atZone(ZoneId.systemDefault()).toInstant());
            }

            List<IpcPredictionForStopPath> result = predictionAnalysisInterface.getCachedTravelTimePredictions(
                    tripId, stopPathIndex, start_date, end_date, algorithm);

            var response = stdParameters.createResponse(new ApiPredictionsForStopPath(result));

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @GetMapping(
        value = "/command/getholdingtime",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    @Operation(
            summary = "Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
            description = "Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
            tags = {"cache"})
    public ResponseEntity<ApiHoldingTime> getHoldingTime(
            StandardParameters stdParameters,
            @Parameter(description = "Stop id", required = true) @RequestParam(value = "stopId") String stopId,
            @Parameter(description = "Vehicle id", required = true) @RequestParam(value = "vehicleId") String vehicleId) {
        try {
            IpcHoldingTime result = holdingTimeInterface.getHoldTime(stopId, vehicleId);

            var response = stdParameters.createResponse(new ApiHoldingTime(result));

            return response;
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }
}
