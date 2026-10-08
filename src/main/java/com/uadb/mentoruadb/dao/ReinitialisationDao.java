package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.ReinitialisationMotDePasse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Demandes de réinitialisation de mot de passe (code envoyé par mail).
 * Le code n'est stocké que haché : cette couche ne voit que le haché.
 */
public class ReinitialisationDao {

    /** Crée une nouvelle demande et annule les demandes précédentes encore actives du même compte. */
    public void creer(int idUtilisateur, String codeHash, LocalDateTime dateExpiration) throws SQLException {
        String annuler = "UPDATE reinitialisation_mot_de_passe SET utilise = 1 "
                + "WHERE id_utilisateur = ? AND utilise = 0";
        String inserer = "INSERT INTO reinitialisation_mot_de_passe "
                + "(id_utilisateur, code_hash, date_expiration, tentatives, utilise, date_creation) "
                + "VALUES (?, ?, ?, 0, 0, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(annuler)) {
                stmt.setInt(1, idUtilisateur);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement(inserer)) {
                stmt.setInt(1, idUtilisateur);
                stmt.setString(2, codeHash);
                stmt.setTimestamp(3, Timestamp.valueOf(dateExpiration));
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
            }
        }
    }

    /** Dernière demande non utilisée du compte (périmée ou non : c'est à l'appelant de vérifier l'expiration). */
    public Optional<ReinitialisationMotDePasse> findDernierNonUtilise(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM reinitialisation_mot_de_passe "
                + "WHERE id_utilisateur = ? AND utilise = 0 "
                + "ORDER BY date_creation DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Incrémente le compteur de tentatives (code erroné). */
    public void incrementerTentatives(int idReinitialisation) throws SQLException {
        String sql = "UPDATE reinitialisation_mot_de_passe SET tentatives = tentatives + 1 "
                + "WHERE id_reinitialisation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReinitialisation);
            stmt.executeUpdate();
        }
    }

    /** Marque la demande comme consommée (mot de passe changé, ou code trop de fois erroné). */
    public void marquerUtilise(int idReinitialisation) throws SQLException {
        String sql = "UPDATE reinitialisation_mot_de_passe SET utilise = 1 WHERE id_reinitialisation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReinitialisation);
            stmt.executeUpdate();
        }
    }

    private ReinitialisationMotDePasse mapRow(ResultSet rs) throws SQLException {
        return new ReinitialisationMotDePasse(
                rs.getInt("id_reinitialisation"),
                rs.getInt("id_utilisateur"),
                rs.getString("code_hash"),
                rs.getTimestamp("date_expiration").toLocalDateTime(),
                rs.getInt("tentatives"),
                rs.getBoolean("utilise"),
                rs.getTimestamp("date_creation").toLocalDateTime()
        );
    }
}
