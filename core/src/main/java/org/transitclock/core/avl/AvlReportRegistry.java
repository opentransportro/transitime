package org.transitclock.core.avl;

import org.springframework.stereotype.Component;
import org.transitclock.domain.structs.AvlReport;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AvlReportRegistry {

    // For keeping track of the last AVL report for each vehicle. Keyed on
    // vehicle ID. Synchronize map modifications since elsewhere the elements
    // can be removed from the map.
    private final Map<String, AvlReport> avlReportsMap = new ConcurrentHashMap<>();

    /**
     * Stores the specified AVL report into map so know the last time received AVL data for the
     * vehicle.
     *
     * @param avlReport AVL report to store
     */
    public void storeAvlReport(AvlReport avlReport) {
        avlReportsMap.put(avlReport.getVehicleId(), avlReport);
    }

    public Collection<AvlReport> avlReportList() {
        return avlReportsMap.values();
    }
}
