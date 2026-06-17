package sn.epf.pointage.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void delete(ID id);
    boolean exists(ID id);
}
