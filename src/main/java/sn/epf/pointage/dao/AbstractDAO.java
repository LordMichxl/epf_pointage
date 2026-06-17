package sn.epf.pointage.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import sn.epf.pointage.config.HibernateConfig;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDAO<T, ID> implements GenericDAO<T, ID> {

    protected final Class<T> entityClass;

    protected AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        return HibernateConfig.getSessionFactory().openSession();
    }

    @Override
    public T save(T entity) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur save() : " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        try (Session session = getSession()) {
            T entity = session.get(entityClass, (java.io.Serializable) id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new RuntimeException("Erreur findById() : " + e.getMessage(), e);
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                          .setMaxResults(100)
                          .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Erreur findAll() : " + e.getMessage(), e);
        }
    }

    @Override
    public T update(T entity) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            T merged = session.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur update() : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(ID id) {
        Transaction tx = null;
        try (Session session = getSession()) {
            tx = session.beginTransaction();
            T entity = session.get(entityClass, (java.io.Serializable) id);
            if (entity != null) session.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Erreur delete() : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(ID id) {
        try (Session session = getSession()) {
            return session.get(entityClass, (java.io.Serializable) id) != null;
        } catch (Exception e) {
            throw new RuntimeException("Erreur exists() : " + e.getMessage(), e);
        }
    }
}
