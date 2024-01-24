/* (C)2023 */
package org.transitclock.core.holdingmethod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ControlStop {
    String stopPathIndex;
    String stopId;

    public ControlStop(String combined) {
        if (combined.contains(":")) {
            String[] splits = combined.split(":");
            stopPathIndex = splits[1];
            stopId = splits[0];
        } else {
            stopId = combined;
            stopPathIndex = null;
        }
    }
}
