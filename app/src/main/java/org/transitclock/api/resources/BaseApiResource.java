package org.transitclock.api.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.transitclock.api.utils.StandardParameters;
import org.transitclock.api.utils.WebUtils;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.service.contract.*;

abstract class BaseApiResource {
    @Autowired
    protected ApiKeyManager manager;

    @Autowired
    protected PredictionsInterface predictionsInterface;

    @Autowired
    protected CacheQueryInterface cacheQueryInterface;

    @Autowired
    protected PredictionAnalysisInterface predictionAnalysisInterface;

    @Autowired
    protected ServerStatusInterface serverStatusInterface;

    @Autowired
    protected VehiclesInterface vehiclesInterface;

    @Autowired
    protected CommandsInterface commandsInterface;

    @Autowired
    protected ConfigInterface configInterface;

    @Autowired
    protected HoldingTimeInterface holdingTimeInterface;

    /**
     * Makes sure not access feed too much and that the key is valid. If there is a problem then
     * throws a WebApplicationException.
     *
     * @throws WebApplicationException
     */
    public void validate(StandardParameters standardParameters) throws WebApplicationException {
        // Make sure the application key is valid
        if (!manager.isKeyValid(standardParameters.getKey())) {
            throw WebUtils.badRequestException(
                    Response.Status.UNAUTHORIZED.getStatusCode(), "Application key \"" + standardParameters.getKey() + "\" is not valid.");
        }
    }
}
