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

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.transitclock.db.structs.ExportTable;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 *
 */
@XmlRootElement
public class ApiExportData {

	@XmlAttribute
	private long id;
	
	@XmlAttribute
	private Date dataDate;
	
	@XmlAttribute
	private Date exportDate;
	
	@XmlAttribute
	private int exportType;
	
	@XmlAttribute
	private int exportStatus;
	
	@XmlAttribute
	private String fileName;
	
	

	
	public ApiExportData() { 
	}

	public ApiExportData(ExportTable exportTable) {
		this.id = exportTable.getId();
		this.exportType = exportTable.getExportType();
		this.exportStatus = exportTable.getExportStatus();
		this.fileName = exportTable.getFileName();
		this.dataDate = exportTable.getDataDate();
		this.exportDate = exportTable.getExportDate();
	}

	public ApiExportData(Long id, 
			Date dataDate, 
			Date exportDate, 
			Integer exportType, 
			Integer exportStatus,
			String fileName) {
		this.id = id;
		this.exportType = exportType;
		this.fileName = fileName;
		this.dataDate = dataDate;
		this.exportDate = exportDate;
		this.exportStatus = exportStatus;
	}
	
}
