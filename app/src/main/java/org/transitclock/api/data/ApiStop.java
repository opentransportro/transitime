/* (C)2023 */
package org.transitclock.api.data;

import org.transitclock.service.dto.IpcStop;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Full description of a stop.
 *
 * <p>Note: extending from ApiLocation since have a lat & lon. Would be nice to have ApiLocation as
 * a member but when try this get a internal server 500 error.
 *
 * @author SkiBu Smith
 */
@Getter @Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ApiStop extends ApiLocation {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private Integer code;

    // For indicating that in UI should de-emphasize this stop because it is not on a main trip pattern.
    @JsonProperty
    private Boolean minor;

    @JsonProperty
    @JsonFormat(shape = Shape.NUMBER)
    private Double pathLength;


    public ApiStop(IpcStop stop) {
        super(stop.getLoc().getLat(), stop.getLoc().getLon());
        this.id = stop.getId();
        this.name = stop.getName();
        this.code = stop.getCode();
        this.pathLength = stop.getStopPathLength() == null ? 0.0 : stop.getStopPathLength();
        // If true then set to null so that this attribute won't then be
        // output as XML/JSON, therefore making output a bit more compact.
        this.minor = stop.isUiStop() ? null : true;
    }
}
