/*
 * This file is part of Transitime.org
 *
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.ipc.data;

import org.transitclock.monitoring.MonitorResult;

import java.io.Serializable;
import java.util.List;

/**
 * Represents server status for Inter Process Communication (IPC)
 *
 * @author SkiBu Smith
 */
public class IpcServerStatus implements Serializable {

    private static final long serialVersionUID = 4167038313695279486L;
    private final List<MonitorResult> monitorResults;

    /********************** Member Functions **************************/

    public IpcServerStatus(List<MonitorResult> monitorResults) {
        this.monitorResults = monitorResults;
    }

    @Override
    public String toString() {
        return "IpcServerStatus ["
                + "monitorResults=" + monitorResults
                + "]";
    }

    public List<MonitorResult> getMonitorResults() {
        return monitorResults;
    }


}
