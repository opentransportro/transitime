/* (C)2023 */
package org.transitclock.core.prediction.accuracy;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.config.data.PredictionAccuracyConfig;
import org.transitclock.utils.PlaybackIntervalTimer;
import org.transitclock.utils.SystemTime;

@Slf4j
public class PlaybackPredictionAccuracyModule extends PredictionAccuracyModule {
    @Override
    public void run() {
        // Run forever
        PlaybackIntervalTimer timer = new PlaybackIntervalTimer();
        while (true) {
            // No need to run at startup since internal predictions won't be
            // generated yet. So sleep a bit first.

            // Time.sleep(5000);
            if (timer.elapsedMsec() > PredictionAccuracyConfig.timeBetweenPollingPredictionsMsec.getValue()) {
                try {
                    // Process data
                    getAndProcessData(getRoutesAndStops(), SystemTime.getDate());

                    // Make sure old predictions that were never matched to an
                    // arrival/departure don't stick around taking up memory.
                    clearStalePredictions();
                } catch (Exception e) {
                    logger.error("Error accessing predictions feed :  {}", e.getMessage(), e);
                }
                timer.resetTimer();
            }
        }
    }
}
