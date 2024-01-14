/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "adherenceSummary")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiAdherenceSummary {

    @XmlAttribute
    private Integer late;

    @XmlAttribute
    private Integer ontime;

    @XmlAttribute
    private Integer early;

    @XmlAttribute
    private Integer nodata;

    @XmlAttribute
    private Integer blocks;
}
