package org.transitclock.core.predictiongenerator.bias;

import org.junit.jupiter.api.Test;
import org.transitclock.utils.Time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ExponentialBiasAdjusterTest {

    @Test
    void adjustPrediction() {
        ExponentialBiasAdjuster adjuster = new ExponentialBiasAdjuster();
        long result = adjuster.adjustPrediction(20 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(3.8637499746628055);
        assertThat(result).isEqualTo(1153635L);

        result = adjuster.adjustPrediction(15 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(2.588624084707828);
        assertThat(result).isEqualTo(876702L);

        result = adjuster.adjustPrediction(10 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(1.7968712300500012);
        assertThat(result).isEqualTo(589218L);

        result = adjuster.adjustPrediction(5 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(1.3052550000000003);
        assertThat(result).isEqualTo(296084L);

        result = adjuster.adjustPrediction(1 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(1.05);
        assertThat(result).isEqualTo(59370L);
    }
}