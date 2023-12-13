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

import java.io.Serializable;
import java.util.Date;

import org.transitclock.db.structs.VehicleToBlockConfig;

/**
 * For transmitting via Interprocess Communication vehicle configuration info. 
 *
 * @author SkiBu Smith
 *
 */
public class IpcVehicleToBlockConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8343324870221439002L;
	
	private final long id;
	private final String vehicleId;
	private final Date validFrom;
	private final Date validTo;
	private final Date assignmentDate;
	private final String blockId;
	private final String tripId;


	
	/********************** Member Functions **************************/

	public IpcVehicleToBlockConfig(VehicleToBlockConfig vehicleToBlockConfig) {
		this.id = vehicleToBlockConfig.getId();
		this.vehicleId = vehicleToBlockConfig.getVehicleId();
		this.validFrom = vehicleToBlockConfig.getValidFrom();
		this.validTo = vehicleToBlockConfig.getValidTo();
		this.assignmentDate = vehicleToBlockConfig.getAssignmentDate();
		this.tripId = vehicleToBlockConfig.getTripId();
		this.blockId = vehicleToBlockConfig.getBlockId();
	}



	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getId() {
		return id;
	}



	public String getVehicleId() {
		return vehicleId;
	}



	public Date getValidFrom() {
		return validFrom;
	}



	public Date getValidTo() {
		return validTo;
	}



	public Date getAssignmentDate() {
		return assignmentDate;
	}



	public String getBlockId() {
		return blockId;
	}



	public String getTripId() {
		return tripId;
	}
}
