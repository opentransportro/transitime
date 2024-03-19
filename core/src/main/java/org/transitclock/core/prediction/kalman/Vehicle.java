/* (C)2023 */
package org.transitclock.core.prediction.kalman;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Vehicle {
    private final String licence;

    public Vehicle(String licence) {
        this.licence = licence;
    }

    public String getLicence() {
        return licence;
    }
}
