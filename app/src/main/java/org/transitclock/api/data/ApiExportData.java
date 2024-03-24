/* (C)2023 */
package org.transitclock.api.data;

import java.util.Date;

import org.transitclock.domain.structs.ExportTable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 */
@Data
public class ApiExportData {

    @JsonProperty
    private long id;

    @JsonProperty
    private Date dataDate;

    @JsonProperty
    private Date exportDate;

    @JsonProperty
    private int exportType;

    @JsonProperty
    private int exportStatus;

    @JsonProperty
    private String fileName;

    public ApiExportData(ExportTable exportTable) {
        this.id = exportTable.getId();
        this.exportType = exportTable.getExportType();
        this.exportStatus = exportTable.getExportStatus();
        this.fileName = exportTable.getFileName();
        this.dataDate = exportTable.getDataDate();
        this.exportDate = exportTable.getExportDate();
    }

    public ApiExportData(Long id, Date dataDate, Date exportDate, Integer exportType, Integer exportStatus, String fileName) {
        this.id = id;
        this.exportType = exportType;
        this.fileName = fileName;
        this.dataDate = dataDate;
        this.exportDate = exportDate;
        this.exportStatus = exportStatus;
    }
}
