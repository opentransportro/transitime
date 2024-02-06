/* (C)2023 */
package org.transitclock.domain.webstructs;

import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.transitclock.domain.hibernate.HibernateUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * Database class for storing keys and related info for the API.
 *
 * @author SkiBu Smith
 */
@Entity
@Data
@Table(name = "api_keys")
public class ApiKey implements Serializable {

    @Column(length = 80)
    @Id
    private final String applicationName;

    @Column(length = 20)
    private final String applicationKey;

    @Column(length = 80)
    private final String applicationUrl;

    @Column(length = 80)
    private final String email;

    @Column(length = 80)
    private final String phone;

    @Column(length = 1000)
    private final String description;

    /**
     * For creating object to be written to db.
     *
     * @param key
     */
    public ApiKey(
            String applicationName, String key, String applicationUrl, String email, String phone, String description) {
        this.applicationName = applicationName;
        this.applicationKey = key;
        this.applicationUrl = applicationUrl;
        this.email = email;
        this.phone = phone;
        this.description = description;
    }

    protected ApiKey() {
        this.applicationName = null;
        this.applicationKey = null;
        this.applicationUrl = null;
        this.email = null;
        this.phone = null;
        this.description = null;
    }

    /**
     * Reads in all keys from database.
     *
     * @param session
     * @return
     * @throws HibernateException
     */
    public static List<ApiKey> getApiKeys(Session session) throws HibernateException {
        var query = session.createQuery("FROM ApiKey", ApiKey.class);
        return query.list();
    }

    /**
     * Stores the ApiKey in the database
     *
     * @param dbName name of database
     */
    public void storeApiKey(String dbName) {
        try (Session session = HibernateUtils.getSession(dbName)) {
            Transaction transaction = session.beginTransaction();
            session.persist(this);
            transaction.commit();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Deletes this ApiKey from specified database
     *
     * @param dbName name of database
     */
    public void deleteApiKey(String dbName) {
        try (Session session = HibernateUtils.getSession(dbName)) {
            session.remove(this);
        } catch (Exception e) {
            throw e;
        }
        // Make sure that the session always gets closed, even if
        // exception occurs
    }
}
