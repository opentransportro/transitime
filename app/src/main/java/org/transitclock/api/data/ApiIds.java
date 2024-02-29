/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.utils.StringUtils;

import java.util.List;

/**
 * For outputting simple list of sorted alpha-number IDs
 *
 * @author SkiBu Smith
 */@Data
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
