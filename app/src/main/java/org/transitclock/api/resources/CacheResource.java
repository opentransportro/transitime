/* (C)2023 */
package org.transitclock.api.resources;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
import org.transitclock.api.utils.WebUtils;
import org.transitclock.service.dto.IpcArrivalDeparture;
import org.transitclock.service.dto.IpcHistoricalAverage;
import org.transitclock.service.dto.IpcHistoricalAverageCacheKey;
import org.transitclock.service.dto.IpcHoldingTime;
import org.transitclock.service.dto.IpcHoldingTimeCacheKey;
import org.transitclock.service.dto.IpcKalmanErrorCacheKey;
import org.transitclock.service.dto.IpcPredictionForStopPath;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contains the API commands for the Transitime API for getting info on data that is cached.
 *
 * <p>The data output can be in either JSON or XML. The output format is specified by the accept
 * header or by using the query string parameter "format=json" or "format=xml".
 *
 * @author SkiBu Smith
 */
@RestController
public class CacheResource extends BaseApiResource implements CacheApi {

    @Override
    public ResponseEntity<ApiKalmanErrorCacheKeysResponse> getKalmanErrorCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcKalmanErrorCacheKey> result = cacheQueryService.getKalmanErrorCacheKeys();
            ApiKalmanErrorCacheKeysResponse keys = new ApiKalmanErrorCacheKeysResponse(result);
            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiHistoricalAverageCacheKeysResponse> getSchedulesBasedHistoricalAverageCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHistoricalAverageCacheKey> result =
                    cacheQueryService.getScheduledBasedHistoricalAverageCacheKeys();

            ApiHistoricalAverageCacheKeysResponse keys = new ApiHistoricalAverageCacheKeysResponse(result);

            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    // TODO This is not completed and should not be used.
    @Override
    public ResponseEntity<ApiHistoricalAverageCacheKeysResponse> getFrequencyBasedHistoricalAverageCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHistoricalAverageCacheKey> result =
                    cacheQueryService.getFrequencyBasedHistoricalAverageCacheKeys();

            ApiHistoricalAverageCacheKeysResponse keys = new ApiHistoricalAverageCacheKeysResponse(result);

            return stdParameters.createResponse(keys);
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiHoldingTimeCacheKeysResponse> getHoldingTimeCacheKeys(StandardParameters stdParameters)
             {
        try {
            List<IpcHoldingTimeCacheKey> result = cacheQueryService.getHoldingTimeCacheKeys();

            ApiHoldingTimeCacheKeysResponse keys = new ApiHoldingTimeCacheKeysResponse(result);

            var response = stdParameters.createResponse(keys);

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiCacheDetails> getCacheInfo(StandardParameters stdParameters, String cachename) {
        try {

            Integer size = cacheQueryService.entriesInCache(cachename);

            if (size != null)
                return stdParameters.createResponse(new ApiCacheDetails(cachename, size));
            else
                throw new Exception("No cache named:" + cachename);

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiArrivalDeparturesResponse> getStopArrivalDepartureCacheData(
            StandardParameters stdParameters,
            @Parameter(description = "Stop Id.", required = true)
            @RequestParam(value = "stopid") String stopid,
            @RequestParam(value = "date") Date date)
             {
        try {

            List<IpcArrivalDeparture> result = cacheQueryService.getStopArrivalDepartures(stopid);

            ApiArrivalDeparturesResponse apiResult = new ApiArrivalDeparturesResponse(result);
            var response = stdParameters.createResponse(apiResult);
            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiArrivalDeparturesResponse> getTripArrivalDepartureCacheData(
            StandardParameters stdParameters,
            String tripid,
            DateParam date,
            Integer starttime)
             {
        try {

            LocalDate queryDate = null;
            if (date != null) queryDate = date.getDate();
            List<IpcArrivalDeparture> result =
                    cacheQueryService.getTripArrivalDepartures(tripid, queryDate, starttime);

            ApiArrivalDeparturesResponse apiResult = new ApiArrivalDeparturesResponse(result);
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
    @Override
    public ResponseEntity<ApiHistoricalAverage> getHistoricalAverageCacheData(
            StandardParameters stdParameters, String tripId,
            Integer stopPathIndex) {
        try {
            IpcHistoricalAverage result = cacheQueryService.getHistoricalAverage(tripId, stopPathIndex);
            var response = stdParameters.createResponse(new ApiHistoricalAverage(result));
            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Double> getKalmanErrorValue(
            StandardParameters stdParameters,
            @Parameter(description = "Trip Id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Stop path index", required = true) @RequestParam(value = "stopPathIndex")
            Integer stopPathIndex) {
        try {

            Double result = cacheQueryService.getKalmanErrorValue(tripId, stopPathIndex);

            var response = stdParameters.createResponse(result);

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override

    public ResponseEntity<ApiPredictionsForStopPathResponse> getStopPathPredictions(
            StandardParameters stdParameters,
            String algorithm,String tripId,
            Integer stopPathIndex,
            DateParam date) {
        try {
            LocalTime midnight = LocalTime.MIDNIGHT;
            Date end_date = null;
            Date start_date = null;
            if (date != null) {
                LocalDate now = date.getDate();

                LocalDateTime todayMidnight = LocalDateTime.of(now, midnight);
                LocalDateTime yesterdatMidnight = todayMidnight.plusDays(-1);

                end_date = Date.from(todayMidnight.atZone(ZoneId.systemDefault()).toInstant());
                start_date = Date.from(yesterdatMidnight.atZone(ZoneId.systemDefault()).toInstant());
            }

            List<IpcPredictionForStopPath> result = predictionAnalysisService.getCachedTravelTimePredictions(
                    tripId, stopPathIndex, start_date, end_date, algorithm);

            var response = stdParameters.createResponse(new ApiPredictionsForStopPathResponse(result));

            return response;

        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ApiHoldingTime> getHoldingTime(StandardParameters stdParameters, String stopId, String vehicleId) {
        try {
            IpcHoldingTime result = holdingTimeService.getHoldTime(stopId, vehicleId);

            var response = stdParameters.createResponse(new ApiHoldingTime(result));

            return response;
        } catch (Exception e) {
            // If problem getting result then return a Bad Request
            throw WebUtils.badRequestException(e.getMessage());
        }
    }
}
