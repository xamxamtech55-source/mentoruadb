package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Ufr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UfrDao implements Dao<Ufr, Integer> {

    @Override
    public Ufr create(Ufr ufr) throws SQLException {
        String sql = "INSERT INTO ufr (nom) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ufr.getNom());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ufr.setIdUfr(keys.getInt(1));
                }
            }
            return ufr;
        }
    }

    @Override
    public Optional<Ufr> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM ufr WHERE id_ufr = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Ufr> findAll() throws SQLException {
        String sql = "SELECT * FROM ufr";
        List<Ufr> resultats = new ArrayList<>();

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
    public void update(Ufr ufr) throws SQLException {
        String sql = "UPDATE ufr SET nom = ? WHERE id_ufr = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ufr.getNom());
            stmt.setInt(2, ufr.getIdUfr());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM ufr WHERE id_ufr = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Ufr mapRow(ResultSet rs) throws SQLException {
        return new Ufr(rs.getInt("id_ufr"), rs.getString("nom"));
    }
}