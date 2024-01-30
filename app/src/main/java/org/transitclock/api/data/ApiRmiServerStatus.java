/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author SkiBu Smith
 */
@XmlRootElement(name = "rmiServerStatus")
public class ApiRmiServerStatus {

    @XmlElement(name = "agency")
    private List<ApiAgencyRmiServerStatus> agenciesData;

    /** Sub API class that actually contains all data for each agency */
    private static class ApiAgencyRmiServerStatus {
    }

    /**
     * Constructors a ApiRmiServerStatus object
     *
     * @param agencyId
     * @param ipcServerStatus
     */
    public ApiRmiServerStatus() {
        agenciesData = new ArrayList<>();

    }
}
