package org.transitclock.core.dataCache;

import org.transitclock.ipc.data.IpcArrivalDeparture;

import java.util.Comparator;


public class IpcArrivalDepartureComparator implements Comparator<IpcArrivalDeparture> {


    @Override
    public int compare(IpcArrivalDeparture ad1, IpcArrivalDeparture ad2) {

        if (ad1.getTime().getTime() < ad2.getTime().getTime()) {
            return 1;
        } else if (ad1.getTime().getTime() > ad2.getTime().getTime()) {
            return -1;
        }
        return 0;
    }

}
