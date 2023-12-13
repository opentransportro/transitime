/* (C)2023 */
package org.transitclock.config;

import static org.junit.Assert.*;

import org.junit.Test;
import org.transitclock.db.webstructs.WebAgency;

public class createWebAgencyTest {

    @Test
    public void testwebagency() {
        try {
            String agencyId = "02";
            String hostName = "jdbc:hsqldb:mem://localhost/xdb";
            boolean active = true;
            String dbName = "02";
            String dbType = "hsql";
            String dbHost = "localhost";
            String dbUserName = "SA";
            String dbPassword = "";
            // Name of database where to store the WebAgency object
            String webAgencyDbName = "web";

            // Create the WebAgency object
            WebAgency webAgency =
                    new WebAgency(agencyId, hostName, active, dbName, dbType, dbHost, dbUserName, dbPassword);
            System.out.println("Storing " + webAgency);

            // Store the WebAgency
            webAgency.store(webAgencyDbName);
            assertTrue(true);
        } catch (Exception e) {
            fail(e.toString());
            e.printStackTrace();
        }
    }
}
