package org.transitclock.api.resources;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.transitclock.api.utils.StandardParameters;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/key/{key}/agency/{agency}")
public interface ReportsApi {
    /**
     * Handles the "tripsWithTravelTimes" command which outputs arrival and departures data for the
     * specified trip by date.
     *
     * @param stdParameters
     * @param date
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/reports/tripsWithTravelTimes",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the arrivals and departures data of a trips.",
            description = "Gets the arrivals and departures data of a trips.",
            tags = {"base data", "trips"})
    ResponseEntity<String> getTripsWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Begin date(YYYY-MM-DD).") @RequestParam(value = "date") String date);

    @Operation(
            summary = "Returns avl report.",
            description = "Returns avl report.",
            tags = {"report", "vehicle"})
    @GetMapping(value = "/reports/avlReport",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<String> getAvlReport(
            StandardParameters stdParameters,
            @Parameter(description = "Vehicle id") @RequestParam(value = "v") String vehicleId,
            @Parameter(description = "Begin date(MM-DD-YYYY.") @RequestParam(value = "beginDate") String beginDate,
            @Parameter(description = "Num days.", required = false) @RequestParam(value = "numDays", required = false) int numDays,
            @Parameter(description = "Begin time(HH:MM)") @RequestParam(value = "beginTime", required = false) String beginTime,
            @Parameter(description = "End time(HH:MM)") @RequestParam(value = "endTime", required = false) String endTime);

    /**
     * Handles the "tripWithTravelTimes" command which outputs arrival and departures data for the
     * specified trip by date.
     *
     * @param stdParameters
     * @param tripId
     * @param date
     *
     * @return
     */
    @GetMapping(value = "/reports/tripWithTravelTimes",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the arrivals and departures data of a trip.",
            description = "Gets the arrivals and departures data of a trip.",
            tags = {"base data", "trip"})
    ResponseEntity<String> getTripWithTravelTimes(
            StandardParameters stdParameters,
            @Parameter(description = "Trip id", required = true) @RequestParam(value = "tripId") String tripId,
            @Parameter(description = "Begin date(YYYY-MM-DD).", required = true) @RequestParam(value = "date") String date);

    /**
     * Handles the "trips" report which outputs trips by date which contains arrival and departures
     * data.
     *
     * @param stdParameters
     * @param date
     *
     * @return
     *
     * @
     */
    @GetMapping(value = "/reports/tripsByDate",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Gets the trips by date.",
            description = "Gets the trips by date.",
            tags = {"base data", "trip"})
    ResponseEntity<String> getTrips(
            StandardParameters stdParameters,
            @Parameter(description = "Date(YYYY-MM-DD).", required = true)
            @RequestParam(value = "date") String date);

    @Operation(
            summary = "Returns schedule adherence report.",
            description = "Returns schedule adherence report.",
            tags = {"report", "route", "schedule adherence"})
    @GetMapping(value = "/reports/scheduleAdh",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<String> scheduleAdhReport(
            StandardParameters stdParameters,
            @Parameter(description = "Route id") @RequestParam(value = "r") String routeId,
            @Parameter(description = "Begin date(MM-DD-YYYY.") @RequestParam(value = "beginDate") String beginDate,
            @Parameter(description = "Num days.", required = false) @RequestParam(value = "numDays", required = false) int numDays,
            @Parameter(description = "Begin time(HH:MM)") @RequestParam(value = "beginTime") String beginTime,
            @Parameter(description = "End time(HH:MM)") @RequestParam(value = "endTime") String endTime,
            @Parameter(description = "Allowable early in mins(default 1.0)")
            @RequestParam(value = "allowableEarly", required = false, defaultValue = "1.0") String allowableEarly,
            @Parameter(description = "Allowable late in mins(default 4.0")
            @RequestParam(value = "allowableLate", required = false, defaultValue = "4.0") String allowableLate);

    @GetMapping(value = "/reports/lastAvlJsonData",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @Operation(
            summary = "Returns AVL Json data for last 24 hours.",
            description = "Returns AVL Json data for last 24 hours.",
            tags = {"report", "avl", "vehicle"})
    ResponseEntity<String> getLastAvlJsonData(StandardParameters stdParameters);

    @GetMapping(value = "/reports/predAccuracyIntervalsData.jsp")
    ResponseEntity<String> predAccuracyIntervalsData(HttpServletRequest request) throws SQLException, ParseException;

    @GetMapping(value = "/reports/predAccuracyRangeData.jsp")
    ResponseEntity<String> predAccuracyRangeData(HttpServletRequest request) throws SQLException, ParseException;

    @GetMapping(value = "/reports/data/summaryScheduleAdherence.jsp")
    ResponseEntity<List<Integer>> summaryScheduleAdherence(HttpServletRequest request) throws ParseException;

    @GetMapping(value = "/reports/predAccuracyScatterData.jsp")
    ResponseEntity<String> predAccuracyScatterData(HttpServletRequest request) throws ParseException, SQLException;
}
