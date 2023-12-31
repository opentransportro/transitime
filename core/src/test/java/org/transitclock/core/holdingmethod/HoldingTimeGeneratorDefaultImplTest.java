/* (C)2023 */
package org.transitclock.core.holdingmethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.transitclock.core.holdingmethod.HoldingTimeGeneratorDefaultImpl.calculateHoldingTime;
import static org.transitclock.core.holdingmethod.HoldingTimeGeneratorDefaultImpl.maxPredictionsForHoldingTimeCalculation;

import org.junit.jupiter.api.Test;

class HoldingTimeGeneratorDefaultImplTest {

    @Test
    void calculateHoldingTimeTest() {
        {
            Long[] N = {11L, 18L, 28L};
            Long result = calculateHoldingTime(5L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(2);
        }
        {
            Long[] N = {9L, 21L, 26L};

            Long result = calculateHoldingTime(5L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(2);
        }
        {
            Long[] N = {10L};
            Long result = calculateHoldingTime(4L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(1);
        }

        {
            Long[] N = {12L, 18L, 25L};
            Long result = calculateHoldingTime(7L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(0);
        }
        {
            Long[] N = {10L, 14L, 19L};
            Long result = calculateHoldingTime(3L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(2);
        }
        {
            Long[] N = {13L, 20L, 28L};
            Long result = calculateHoldingTime(4L, 0L, N, maxPredictionsForHoldingTimeCalculation.getValue());
            assertThat(result).isEqualTo(3);
        }
    }
}
