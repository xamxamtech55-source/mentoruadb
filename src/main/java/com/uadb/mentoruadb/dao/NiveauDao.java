package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Niveau;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NiveauDao implements Dao<Niveau, Integer> {

    @Override
    public Niveau create(Niveau n) throws SQLException {
        String sql = "INSERT INTO niveau (libelle) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, n.getLibelle());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    n.setIdNiveau(keys.getInt(1));
                }
            }
            return n;
        }
    }

    @Override
    public Optional<Niveau> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM niveau WHERE id_niveau = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Niveau> findAll() throws SQLException {
        String sql = "SELECT * FROM niveau";
        List<Niveau> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
        }
        return resultats;
    }

    @Override
    public void update(Niveau n) throws SQLException {
        String sql = "UPDATE niveau SET libelle = ? WHERE id_niveau = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, n.getLibelle());
            stmt.setInt(2, n.getIdNiveau());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM niveau WHERE id_niveau = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Niveau mapRow(ResultSet rs) throws SQLException {
        return new Niveau(rs.getInt("id_niveau"), rs.getString("libelle"));
    }

}