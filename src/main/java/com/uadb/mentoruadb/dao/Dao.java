package com.uadb.mentoruadb.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrat générique pour toutes les couches d'accès aux données.
 * T = le type d'entité (Utilisateur, Mentor, etc.)
 * ID = le type de la clé primaire (Integer dans notre cas)
 */
public interface Dao<T, ID> {

    T create(T entity) throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    void update(T entity) throws SQLException;

    void delete(ID id) throws SQLException;
}