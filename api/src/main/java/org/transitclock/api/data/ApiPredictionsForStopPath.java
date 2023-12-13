/* (C)2023 */
package org.transitclock.api.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.ipc.data.IpcPredictionForStopPath;

/**
 * An ordered list of routes.
 *
 * @author SkiBu Smith
 */
@XmlRootElement(name = "predictions")
public class ApiPredictionsForStopPath {

    @XmlElement(name = "prediction")
    private List<ApiPredictionForStopPath> predictionsForStopPath;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiPredictionsForStopPath() {}

    /**
     * Constructs an ApiRouteSummaries using a collection of IpcRouteSummary objects.
     *
     * @param routes
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ApiPredictionsForStopPath(Collection<IpcPredictionForStopPath> predictions)
            throws IllegalAccessException, InvocationTargetException {
        predictionsForStopPath = new ArrayList<ApiPredictionForStopPath>();
        for (IpcPredictionForStopPath prediction : predictions) {
            ApiPredictionForStopPath apiPredictionForStopPath = new ApiPredictionForStopPath(prediction);
            predictionsForStopPath.add(apiPredictionForStopPath);
        }
    }
}
