/* (C)2023 */
package org.transitclock.core.predAccuracy;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.applications.Core;
import org.transitclock.utils.PlaybackIntervalTimer;

@Slf4j
public class PlaybackPredictionAccuracyModule extends PredictionAccuracyModule {
    public PlaybackPredictionAccuracyModule(String agencyId) {
        super(agencyId);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        // Log that module successfully started
        logger.info("Started module {} for agencyId={}", getClass().getName(), getAgencyId());

        // Run forever
        PlaybackIntervalTimer timer = new PlaybackIntervalTimer();
        while (true) {
            // No need to run at startup since internal predictions won't be
            // generated yet. So sleep a bit first.

            // Time.sleep(5000);
            if (timer.elapsedMsec() > timeBetweenPollingPredictionsMsec.getValue()) {
                try {
                    // Process data
                    getAndProcessData(getRoutesAndStops(), Core.getInstance().getSystemDate());

                    // Make sure old predictions that were never matched to an
                    // arrival/departure don't stick around taking up memory.
                    clearStalePredictions();
                } catch (Exception e) {
                    logger.error("Error accessing predictions feed :  " + e.getMessage(), e);
                }
                timer.resetTimer();
            }
        }
    }
}
