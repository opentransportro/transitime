package org.transitclock.api.resources;

import java.util.Date;

import org.transitclock.api.data.ApiArrivalDeparturesResponse;
import org.transitclock.api.data.ApiCacheDetails;
import org.transitclock.api.data.ApiHistoricalAverage;
import org.transitclock.api.data.ApiHistoricalAverageCacheKeysResponse;
import org.transitclock.api.data.ApiHoldingTime;
import org.transitclock.api.data.ApiHoldingTimeCacheKeysResponse;
import org.transitclock.api.data.ApiKalmanErrorCacheKeysResponse;
import org.transitclock.api.data.ApiPredictionsForStopPathResponse;
import org.transitclock.api.resources.request.DateParam;
import org.transitclock.api.utils.StandardParameters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public interface CacheApi {
    @GetMapping(
            value = "/command/kalmanerrorcachekeys",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Gets the list of Kalman Cache error.",
            description = "Gets the list of Kalman Cache error.",
            tags = {"kalman", "cache"})
    ResponseEntity<ApiKalmanErrorCacheKeysResponse> getKalmanErrorCacheKeys(StandardParameters stdParameters);

    @GetMapping(
            value = "/command/scheduledbasedhistoricalaveragecachekeys",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Gets a list of the keys that have values in the historical average cache for"
                    + " schedules based services.",
            description = "Gets a list of the keys that have values in the historical average cache for"
                    + " schedules based services.",
            tags = {"cache"})
    ResponseEntity<ApiHistoricalAverageCacheKeysResponse> getSchedulesBasedHistoricalAverageCacheKeys(StandardParameters stdParameters);

    // TODO This is not completed and should not be used.
    @GetMapping(
            value = "/command/frequencybasedhistoricalaveragecachekeys",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Gets a list of the keys that have values in the historical average cache for"
                    + " frequency based services.",
            description = "Gets a list of the keys that have values in the historical average cache for"
                    + " frequency based services.<font color=\"#FF0000\">This is not completed"
                    + " and should not be used.<font>",
            tags = {"cache"})
    ResponseEntity<ApiHistoricalAverageCacheKeysResponse> getFrequencyBasedHistoricalAverageCacheKeys(StandardParameters stdParameters);

    @GetMapping(
            value = "/command/holdingtimecachekeys",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Gets a list of the keys for the holding times in the cache.",
            description = "Gets a list of the keys for the holding times in the cache.",
            tags = {"cache"})
    ResponseEntity<ApiHoldingTimeCacheKeysResponse> getHoldingTimeCacheKeys(StandardParameters stdParameters);

    /**
     * Returns info about a cache.
     *
     * @param stdParameters
     * @param cachename     this is the name of the cache to get the size of.
     *
     * @return
     *
     * @
     */
    @GetMapping(
            value = "/command/cacheinfo",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns the number of entries in the cacheName cache.",
            description = "Returns the number of entries in the cacheName cache. The name is passed"
                    + " throug the cachename parameter.",
            tags = {"cache"})
    ResponseEntity<ApiCacheDetails> getCacheInfo(
            StandardParameters stdParameters,
            @Parameter(description = "Name of the cache", required = true) @RequestParam(value = "cachename")
            String cachename);

    @GetMapping(
            value = "/command/stoparrivaldeparturecachedata",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns a list of current arrival or departure events for a specified stop"
                    + " that are in the cache.",
            description = "Returns a list of current arrival or departure events for a specified stop"
                    + " that are in the cache.",
            tags = {"cache"})
    ResponseEntity<ApiArrivalDeparturesResponse> getStopArrivalDepartureCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "Stop Id.", required = true)
            @RequestParam(value = "stopid") String stopid,
            @RequestParam(value = "date") Date date);

    @GetMapping(
            value = "/command/triparrivaldeparturecachedata",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns the arrivals and departures for a trip on a specific day and start" + " time.",
            description = "Returns a list  of the arrivals and departures for a trip on a specific day"
                    + " and start time.Either tripId or date must be specified.",
            tags = {"cache"})
    ResponseEntity<ApiArrivalDeparturesResponse> getTripArrivalDepartureCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "if specified, returns the list for that tripId.", required = false)
            @RequestParam(value = "tripId")
            String tripid,
            @Parameter(description = "if specified, returns the list for that date.", required = false)
            @RequestParam(value = "date")
            DateParam date,
            @Parameter(description = "if specified, returns the list for that starttime.", required = false)
            @RequestParam(value = "starttime")
            Integer starttime);

    /*
     * This will give the historical cache value for an individual stop path
     * index of a trip private String tripId; private Integer stopPathIndex;
     */
    @GetMapping(
            value = "/command/historicalaveragecachedata",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns the historical cache value for an individual stop path index of a" + " trip.",
            description = "Returns the historical cache value for an individual stop path index of a" + " trip.",
            tags = {"cache"})
    ResponseEntity<ApiHistoricalAverage> getHistoricalAverageCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
            Integer stopPathIndex);

    @GetMapping(
            value = "/command/getkalmanerrorvalue",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns the latest Kalman error value for a the stop path of a trip.",
            description = "Returns the latest Kalman error value for a the stop path of a trip.",
            tags = {"kalman", "cache"})
    ResponseEntity<Double> getKalmanErrorValue(
            StandardParameters stdParameters,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
            Integer stopPathIndex);

    @GetMapping(
            value = "/command/getstoppathpredictions",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns a list of predictions for a the stop path of a trip.",
            description = "Returns a list of predictions for a the stop path of a trip.",
            tags = {"cache"})
        // TODO: (vsperez) I believe date is not used at all
    ResponseEntity<ApiPredictionsForStopPathResponse> getStopPathPredictions(
            StandardParameters stdParameters,
            @Parameter(description = "Algorith used for calculating the perdiction", required = false)
            @RequestParam(value = "algorithm")
            String algorithm,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
            Integer stopPathIndex,
            @Parameter(description = "Specified the date.", required = true) @RequestParam(value = "date")
            DateParam date);

    @GetMapping(
            value = "/command/getholdingtime",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    @Operation(
            summary = "Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
            description = "Returns the IpcHoldingTime for a specific stop Id and vehicle Id.",
            tags = {"cache"})
    ResponseEntity<ApiHoldingTime> getHoldingTime(
            StandardParameters stdParameters,
            @Parameter(description = "Stop id", required = true) @RequestParam(value = "stopId") String stopId,
            @Parameter(description = "Vehicle id", required = true) @RequestParam(value = "vehicleId") String vehicleId);
}
