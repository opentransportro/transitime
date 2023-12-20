/* (C)2023 */
package org.transitclock.monitoring;

import java.util.List;
import org.hibernate.HibernateException;
import org.transitclock.core.domain.DbTest;
import org.transitclock.utils.EmailSender;

/**
 * For monitoring access to database. Makes sure can read and write to database.
 *
 * @author SkiBu Smith
 */
public class DatabaseMonitor extends MonitorBase {

    /********************** Member Functions **************************/

    /**
     * Simple constructor
     *
     * @param emailSender
     * @param agencyId
     */
    public DatabaseMonitor(EmailSender emailSender, String agencyId) {
        super(emailSender, agencyId);
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#triggered()
     */
    @Override
    protected boolean triggered() {
        try {
            // Clear out old data from db
            DbTest.deleteAll(agencyId);

            // See if can write an object to database
            if (!DbTest.write(agencyId, 999)) {
                setMessage("Could not write DbTest object to database.");
                return true;
            }

            // See if can read object from database
            List<DbTest> dbTests = DbTest.readAll(agencyId);
            if (dbTests.size() == 0) {
                setMessage("Could not read DbTest objects from database.");
                return true;
            }
        } catch (HibernateException e) {
            setMessage("Problem accessing database. " + e.getMessage());
            return true;
        }

        // Everything OK
        setMessage("Successfully read and wrote to database.");
        return false;
    }

    /* (non-Javadoc)
     * @see org.transitclock.monitoring.MonitorBase#type()
     */
    @Override
    protected String type() {
        return "Database";
    }
}
