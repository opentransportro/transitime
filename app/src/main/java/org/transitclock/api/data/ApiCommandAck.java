/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "acknowledgment")
public class ApiCommandAck {
    @XmlAttribute
    private boolean success;

    @XmlAttribute
    private String message;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiCommandAck() {}

    public ApiCommandAck(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
