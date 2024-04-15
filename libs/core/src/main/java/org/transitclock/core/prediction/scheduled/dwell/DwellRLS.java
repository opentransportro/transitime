/* (C)2023 */
package org.transitclock.core.prediction.scheduled.dwell;

import org.transitclock.ApplicationProperties.Prediction;
import org.transitclock.core.prediction.scheduled.dwell.rls.TransitClockRLS;

/**
 * @author scrudden
 */
public class DwellRLS implements DwellModel {
    private TransitClockRLS rls;

    public DwellRLS(Prediction.Rls rlsProperties) {
        rls = new TransitClockRLS(rlsProperties.getLambda());
    }

    public TransitClockRLS getRls() {
        return rls;
    }

    public void setRls(TransitClockRLS rls) {
        this.rls = rls;
    }

    @Override
    public Integer predict(Integer headway, Integer demand) {
        double[] arg0 = new double[1];
        arg0[0] = headway;
        if (rls.getRls() != null) return (int) Math.pow(10, rls.getRls().predict(arg0));
        else return null;
    }

    @Override
    public void putSample(Integer dwelltime, Integer headway, Integer demand) {
        rls.addSample(headway, Math.log10(dwelltime));
    }
}
