/* (C)2023 */
package org.transitclock.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "adherenceSummary")
@Data
@AllArgsConstructor
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
