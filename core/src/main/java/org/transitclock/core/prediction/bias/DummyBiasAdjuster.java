package org.transitclock.core.prediction.bias;

public class DummyBiasAdjuster implements BiasAdjuster{
    @Override
    public long adjustPrediction(long prediction) {
        return prediction;
    }
}
