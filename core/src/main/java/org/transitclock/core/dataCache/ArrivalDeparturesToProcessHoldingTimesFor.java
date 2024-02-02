/* (C)2023 */
package org.transitclock.core.dataCache;

import lombok.extern.slf4j.Slf4j;
import org.transitclock.domain.structs.ArrivalDeparture;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Óg Crudden
 */
@Slf4j
public class ArrivalDeparturesToProcessHoldingTimesFor {

    private final ArrayList<ArrivalDeparture> m;

    public ArrivalDeparturesToProcessHoldingTimesFor() {
        m = new ArrayList<>();
    }

    public void empty() {
        m.clear();
    }

    public void add(ArrivalDeparture ad) {
        m.add(ad);
    }

    public List<ArrivalDeparture> getList() {
        return m;
    }
}
