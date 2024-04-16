package org.transitclock.api.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.ApplicationProperties;
import org.transitclock.api.exception.InvalidAccessException;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.service.contract.*;

public abstract class BaseApiResource {
    @Autowired
    protected ApplicationProperties properties;

    @Autowired
    protected ApiKeyManager manager;

    @Autowired
    protected PredictionsService predictionsService;

    @Autowired
    protected CacheQueryService cacheQueryService;

    @Autowired
    protected PredictionAnalysisService predictionAnalysisService;

    @Autowired
    protected VehiclesService vehiclesService;

    @Autowired
    protected CommandsService commandsService;

    @Autowired
    protected ConfigService configService;

    @Autowired
    protected HoldingTimeService holdingTimeService;

    /**
     * Makes sure not access feed too much and that the key is valid. If there is a problem then
     * throws a WebApplicationException.
     *
     * @throws InvalidAccessException
     */

    public void validate(StandardParameters standardParameters) {
        // Make sure the application key is valid
        if (!manager.isKeyValid(standardParameters.getKey())) {
            throw new InvalidAccessException();
        }
    }
}
