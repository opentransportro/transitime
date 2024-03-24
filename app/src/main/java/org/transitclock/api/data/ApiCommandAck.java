/* (C)2023 */
package org.transitclock.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApiCommandAck {
    @JsonProperty
    private boolean success;

    @JsonProperty
    private String message;

    public ApiCommandAck(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
