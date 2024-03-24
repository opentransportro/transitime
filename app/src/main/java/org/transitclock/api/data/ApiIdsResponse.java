/* (C)2023 */
package org.transitclock.api.data;

import java.util.List;

import org.transitclock.utils.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For outputting simple list of sorted alpha-number IDs
 *
 * @author SkiBu Smith
 */
@Data
public class ApiIdsResponse {

    @JsonProperty("data")
    private List<String> data;

    /**
     * Creates the API sorted version of list of IDs.
     *
     * @param ids
     */
    public ApiIdsResponse(List<String> ids) {
        this.data = ids;
        this.data.sort((o1, o2) -> {
            String paddedStr1 = StringUtils.paddedName(o1);
            String paddedStr2 = StringUtils.paddedName(o2);

            return paddedStr1.compareTo(paddedStr2);
        });
    }
}
