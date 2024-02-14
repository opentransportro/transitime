package org.transitclock.core.predictiongenerator.bias;

import org.junit.jupiter.api.Test;
import org.transitclock.utils.Time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LinearBiasAdjusterTest {

    @Test
    void adjustPrediction() {
        LinearBiasAdjuster adjuster = new LinearBiasAdjuster(0.0006);
        long result = adjuster.adjustPrediction(20 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(7.199999999999999);
        assertThat(result).isEqualTo(1113600L);

        result = adjuster.adjustPrediction(15 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(5.3999999999999995);
        assertThat(result).isEqualTo(851400L);

        result = adjuster.adjustPrediction(10 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(3.5999999999999996);
        assertThat(result).isEqualTo(578400L);

        result = adjuster.adjustPrediction(5 * Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(1.7999999999999998);
        assertThat(result).isEqualTo(294600L);

        result = adjuster.adjustPrediction(Time.MS_PER_MIN);
        assertThat(adjuster.getPercentage()).isEqualTo(0.36);
        assertThat(result).isEqualTo(59784L);
    }
}