/* (C)2023 */
package org.transitclock.api.data;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.transitclock.db.structs.ExportTable;

/**
 * For when have list of exports. By using this class can control the element name when data is
 * output.
 *
 * @author Hubert goEuropa
 */
@XmlRootElement
public class ApiExportsData implements Serializable {

    /** */
    private static final long serialVersionUID = -9106451522038837974L;

    @XmlElement(name = "exports")
    private List<ApiExportData> exportsData;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    public ApiExportsData() {}

    /**
     * For constructing a ApiVehicles object from a Collection of Vehicle objects.
     *
     * @param vehicles
     * @param uiTypesForVehicles Specifies how vehicles should be drawn in UI. Can be NORMAL,
     *     SECONDARY, or MINOR
     */
    public ApiExportsData(List<ExportTable> exportData) {
        exportsData = new ArrayList<ApiExportData>();
        /*for (ExportTable oneExportData : exportData ) {
        	// Determine UI type for vehicle
        	exportsData.add(new ApiExportData(oneExportData));
        }*/
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
        Iterator itr = exportData.iterator();
        while (itr.hasNext()) {
            Object[] obj = (Object[]) itr.next();
            // now you have one array of Object for each row
            exportsData.add(new ApiExportData(
                    Long.valueOf(obj[0].toString()),
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
