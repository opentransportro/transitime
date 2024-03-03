/* (C)2023 */
package org.transitclock.core.prediction.bias;

/**
 * @author scrudden Implement this to add bias adjustment if needed.
 */
public interface BiasAdjuster {
    long adjustPrediction(long prediction);
}
