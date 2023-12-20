/* (C)2023 */
package org.transitclock.core.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 */
@Setter
@Getter
@Document(collection = "ExportTable")
public class ExportTable implements Serializable {

    @Id
    private long id;

    private Date dataDate;

    private Date exportDate;

    private int exportType;

    private int exportStatus;

    private String fileName;

    private byte[] file;

    public ExportTable(Date dataDate, int exportType, String fileName) {
        this.dataDate = dataDate;
        this.exportType = exportType;
        this.fileName = fileName;
        this.exportDate = new Date();
        this.exportStatus = 1;
    }

    public ExportTable(long id, Date dataDate, Date exportDate, int exportType, int exportStatus, String fileName) {
        this.id = id;
        this.dataDate = dataDate;
        this.exportDate = exportDate;
        this.exportType = exportType;
        this.exportStatus = exportStatus;
        this.fileName = fileName;
    }

}
