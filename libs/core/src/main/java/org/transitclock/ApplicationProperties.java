package org.transitclock;


import org.transitclock.properties.ApiProperties;
import org.transitclock.properties.ArrivalsDeparturesProperties;
import org.transitclock.properties.AutoBlockAssignerProperties;
import org.transitclock.properties.AvlProperties;
import org.transitclock.properties.CoreProperties;
import org.transitclock.properties.GtfsProperties;
import org.transitclock.properties.HoldingProperties;
import org.transitclock.properties.MonitoringProperties;
import org.transitclock.properties.PredictionAccuracyProperties;
import org.transitclock.properties.PredictionProperties;
import org.transitclock.properties.ServiceProperties;
import org.transitclock.properties.TimeoutProperties;
import org.transitclock.properties.TravelTimesProperties;
import org.transitclock.properties.TripDataCacheProperties;
import org.transitclock.properties.UpdatesProperties;
import org.transitclock.properties.WebProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "transitclock")
public class ApplicationProperties {

    private ApiProperties api = new ApiProperties();

    private ArrivalsDeparturesProperties arrivalsDepartures = new ArrivalsDeparturesProperties();

    private AvlProperties avl = new AvlProperties();

    private AutoBlockAssignerProperties autoBlockAssigner = new AutoBlockAssignerProperties();

    private CoreProperties core = new CoreProperties();

    private GtfsProperties gtfs = new GtfsProperties();

    private HoldingProperties holding = new HoldingProperties();

    private MonitoringProperties monitoring = new MonitoringProperties();

    private PredictionAccuracyProperties predAccuracy = new PredictionAccuracyProperties();

    private PredictionProperties prediction = new PredictionProperties();

    private ServiceProperties service =new ServiceProperties();

    private TimeoutProperties timeout = new TimeoutProperties();

    private TravelTimesProperties travelTimes = new TravelTimesProperties();

    private TripDataCacheProperties tripDataCache = new TripDataCacheProperties();

    private UpdatesProperties updates = new UpdatesProperties();

    private WebProperties web = new WebProperties();
}
