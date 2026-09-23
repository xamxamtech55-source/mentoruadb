package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Etudiant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantDao implements Dao<Etudiant, Integer> {

    @Override
    public Etudiant create(Etudiant e) throws SQLException {
        String sql = "INSERT INTO etudiant (id_utilisateur, id_filiere, id_niveau, numero_carte) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, e.getIdUtilisateur());
            stmt.setInt(2, e.getIdFiliere());
            stmt.setInt(3, e.getIdNiveau());
            stmt.setString(4, e.getNumeroCarte());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setIdEtudiant(keys.getInt(1));
                }
            }
            return e;
        }
    }

    @Override
    public Optional<Etudiant> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM etudiant WHERE id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Utile pour retrouver le profil étudiant d'un compte utilisateur connecté (relation 1-1). */
    public Optional<Etudiant> findByUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM etudiant WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Etudiant> findAll() throws SQLException {
        String sql = "SELECT * FROM etudiant";
        List<Etudiant> resultats = new ArrayList<>();

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
    public void update(Etudiant e) throws SQLException {
        String sql = "UPDATE etudiant SET id_utilisateur = ?, id_filiere = ?, id_niveau = ?, numero_carte = ? WHERE id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, e.getIdUtilisateur());
            stmt.setInt(2, e.getIdFiliere());
            stmt.setInt(3, e.getIdNiveau());
            stmt.setString(4, e.getNumeroCarte());
            stmt.setInt(5, e.getIdEtudiant());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM etudiant WHERE id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Etudiant mapRow(ResultSet rs) throws SQLException {
        return new Etudiant(
                rs.getInt("id_etudiant"),
                rs.getInt("id_utilisateur"),
                rs.getInt("id_filiere"),
                rs.getInt("id_niveau"),
                rs.getString("numero_carte")
        );
    }
}