/* (C)2023 */
package org.transitclock.api.reports;

import org.transitclock.config.BooleanConfigValue;

public class ReportsConfig {

    private static BooleanConfigValue showPredictionSource = new BooleanConfigValue(
            "transitclock.reports.showPredictionSource",
            true,
            "Whether prediction source UI element should be visible.");

    public static boolean isShowPredictionSource() {
        return showPredictionSource.getValue();
    }
}
