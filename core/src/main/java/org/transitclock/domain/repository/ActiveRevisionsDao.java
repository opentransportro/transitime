package org.transitclock.domain.repository;

import org.springframework.stereotype.Repository;
import org.transitclock.domain.structs.ActiveRevisions;

@Repository
public class ActiveRevisionsDao extends BaseDao<ActiveRevisions> {
    public ActiveRevisions get() {
        ActiveRevisions activeRevisions;
        try (var session = session()) {
            // There should only be a single object so don't need a WHERE clause
            var query = session.createQuery("FROM ActiveRevisions", ActiveRevisions.class);
            activeRevisions = null;
            try {
                activeRevisions = query.uniqueResult();
            } catch (Exception e) {
                System.err.println("Exception when reading ActiveRevisions object " + "from database so will create it");
            } finally {
                // If we couldn't read from db use default values and write the object to the database.
                if (activeRevisions == null) {
                    activeRevisions = new ActiveRevisions();
                    session.persist(activeRevisions);
                }
            }
        }

        // Return the object
        return activeRevisions;
    }
}
