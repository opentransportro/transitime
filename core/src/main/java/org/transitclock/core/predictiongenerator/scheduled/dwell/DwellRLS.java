/* (C)2023 */
package org.transitclock.core.predictiongenerator.scheduled.dwell;

import java.io.Serializable;
import org.transitclock.config.DoubleConfigValue;
import org.transitclock.configData.PredictionConfig;
import org.transitclock.core.predictiongenerator.scheduled.dwell.rls.TransitClockRLS;

/**
 * @author scrudden
 */
public class DwellRLS implements DwellModel, Serializable {

    /** */
    private static final long serialVersionUID = -9082591970192068672L;

    private TransitClockRLS rls = null;

    public TransitClockRLS getRls() {
        return rls;
    }

    public void setRls(TransitClockRLS rls) {
        this.rls = rls;
    }

    public DwellRLS() {
        super();
        rls = new TransitClockRLS(PredictionConfig.lambda.getValue());
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
