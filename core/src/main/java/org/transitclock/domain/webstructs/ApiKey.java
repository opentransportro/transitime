/* (C)2023 */
package org.transitclock.domain.webstructs;

import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@Table(name = "api_keys")
public class ApiKey implements Serializable {

    @Column(name = "application_name", length = 80)
    @Id
    private String applicationName;

    @Column(name = "application_key", length = 20)
    private String applicationKey;

    @Column(name = "application_url", length = 80)
    private String applicationUrl;

    @Column(name = "email", length = 80)
    private String email;

    @Column(name = "phone", length = 80)
    private String phone;

    @Column(name = "description", length = 1000)
    private String description;

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
        }
    }
}
