package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilisateurDao implements Dao<Utilisateur, Integer> {

    @Override
    public Utilisateur create(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, statut, telephone, photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getMotDePasse());
            stmt.setString(5, u.getRole());
            stmt.setString(6, u.getStatut());
            stmt.setString(7, u.getTelephone());
            stmt.setString(8, u.getPhoto());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    u.setIdUtilisateur(keys.getInt(1));
                }
            }
            return u;
        }
    }

    @Override
    public Optional<Utilisateur> findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Utilisateur> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Utilisateur> findAll() throws SQLException {
        String sql = "SELECT * FROM utilisateur";
        List<Utilisateur> resultats = new ArrayList<>();

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
    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, role=?, statut=?, " +
                "telephone=?, photo=? WHERE id_utilisateur=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getMotDePasse());
            stmt.setString(5, u.getRole());
            stmt.setString(6, u.getStatut());
            stmt.setString(7, u.getTelephone());
            stmt.setString(8, u.getPhoto());
            stmt.setInt(9, u.getIdUtilisateur());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("id_utilisateur"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("role"),
                rs.getString("statut"),
                rs.getString("telephone"),
                rs.getString("photo")
        );
    }
}