package org.transitclock.domain.repository;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Transactional
public class BaseDao<T> {
    @Autowired
    private SessionFactory sessionFactory;

//    @Autowired
//    private TransactionManager transactionManager;

    public Session session() {
        return sessionFactory.getCurrentSession();
    }

    public void flush() {
        session().flush();
    }

    /**
     * save or update the entity without authorizing write access
     *
     * @param entity
     * @return
     */
    public T saveOrUpdate(T entity) {
        final Session session = session();
        session.saveOrUpdate(entity);
        return entity;
    }
}
