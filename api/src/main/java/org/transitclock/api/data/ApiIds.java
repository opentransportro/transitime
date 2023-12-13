/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.transitclock.utils.StringUtils;

/**
 * For outputting simple list of sorted alpha-number IDs
 *
 * @author SkiBu Smith
 */
@XmlRootElement
public class ApiIds {

    @XmlElement
    private List<String> ids;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiIds() {}

    /**
     * Creates the API sorted version of list of IDs.
     *
     * @param ids
     */
    public ApiIds(List<String> ids) {
        StringUtils.sortIdsNumerically(ids);
        this.ids = ids;
    }
}
