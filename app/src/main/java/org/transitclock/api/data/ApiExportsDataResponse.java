/* (C)2023 */
package org.transitclock.api.data;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.transitclock.domain.structs.ExportTable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * For when have list of exports. By using this class can control the element name when data is
 * output.
 *
 * @author Hubert goEuropa
 */
@Data
public class ApiExportsDataResponse implements Serializable {

    @JsonProperty("data")
    private List<ApiExportData> data;

    public ApiExportsDataResponse(List<ExportTable> exportData) {
        data = new ArrayList<>();
        for (ExportTable oneExportData : exportData ) {
        	data.add(new ApiExportData(oneExportData));
        }
    }
}
