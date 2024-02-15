package org.transitclock.core.predictiongenerator.bias;

public class DummyBiasAdjuster implements BiasAdjuster{
    @Override
    public long adjustPrediction(long prediction) {
        return prediction;
    }
}
