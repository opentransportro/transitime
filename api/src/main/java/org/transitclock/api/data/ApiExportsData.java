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

package org.transitclock.api.data;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.transitclock.api.rootResources.TransitimeApi.UiMode;
import org.transitclock.db.structs.ExportTable;
import org.transitclock.ipc.data.IpcVehicle;

/**
 * For when have list of exports. By using this class can control the element
 * name when data is output.
 *
 * @author Hubert goEuropa
 *
 */
@XmlRootElement
public class ApiExportsData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9106451522038837974L;
	@XmlElement(name = "exports")
	private List<ApiExportData> exportsData;

	/********************** Member Functions **************************/

	/**
	 * Need a no-arg constructor for Jersey. Otherwise get really obtuse
	 * "MessageBodyWriter not found for media type=application/json" exception.
	 */
	public ApiExportsData() {
	}

	/**
	 * For constructing a ApiVehicles object from a Collection of Vehicle
	 * objects.
	 * 
	 * @param vehicles
	 * @param uiTypesForVehicles
	 *            Specifies how vehicles should be drawn in UI. Can be NORMAL,
	 *            SECONDARY, or MINOR
	 */
	public ApiExportsData(List<ExportTable> exportData) {
		exportsData = new ArrayList<ApiExportData>();
		/*for (ExportTable oneExportData : exportData ) {
			// Determine UI type for vehicle
			exportsData.add(new ApiExportData(oneExportData));
		}*/
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
		Iterator itr = exportData.iterator();
		while(itr.hasNext()){
		   Object[] obj = (Object[]) itr.next();
		   //now you have one array of Object for each row
		   exportsData.add(new ApiExportData(Long.valueOf(obj[0].toString()),
				   Date.valueOf(obj[1].toString()),
				   DateTime.parse(obj[2].toString(), formatter).toDate(),
				   Integer.valueOf(obj[3].toString()),
				   Integer.valueOf(obj[4].toString()),
				   String.valueOf(obj[5])));
		}
	}

	public List<ApiExportData> getExportsData() {
		return exportsData;
	}

	public void setExportsData(List<ApiExportData> exportsData) {
		this.exportsData = exportsData;
	}
	
	

}
