package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Matiere;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatiereDao implements Dao<Matiere, Integer> {

    @Override
    public Matiere create(Matiere m) throws SQLException {
        String sql = "INSERT INTO matiere (nom) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, m.getNom());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    m.setIdMatiere(keys.getInt(1));
                }
            }
            return m;
        }
    }

    @Override
    public Optional<Matiere> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM matiere WHERE id_matiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Matiere> findAll() throws SQLException {
        String sql = "SELECT * FROM matiere";
        List<Matiere> resultats = new ArrayList<>();

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
    public void update(Matiere m) throws SQLException {
        String sql = "UPDATE matiere SET nom = ? WHERE id_matiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getNom());
            stmt.setInt(2, m.getIdMatiere());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM matiere WHERE id_matiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Matiere mapRow(ResultSet rs) throws SQLException {
        return new Matiere(rs.getInt("id_matiere"), rs.getString("nom"));
    }
}