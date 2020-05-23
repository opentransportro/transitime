package org.transitclock.api.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "servertime")
public class ApiCurrentServerDate {
    @XmlAttribute
    private Date currentTime;

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse
     * "MessageBodyWriter not found for media type=application/json" exception.
     */
    protected ApiCurrentServerDate() {
    }

    public ApiCurrentServerDate(Date currentTime) {
        this.currentTime = currentTime;
    }
}
