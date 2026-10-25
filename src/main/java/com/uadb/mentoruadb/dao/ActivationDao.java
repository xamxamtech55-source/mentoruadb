package com.uadb.mentoruadb.dao;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.ActivationCompte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Demandes d'activation de compte. Le code et le ticket ne sont vus ici que
 * sous forme hachée.
 */
public class ActivationDao {

    /** Crée une demande dans la transaction fournie (l'appelant gère commit/rollback). */
    public ActivationCompte create(ActivationCompte activation, Connection conn) throws SQLException {
        String sql = "INSERT INTO activation_compte "
                + "(id_referentiel, id_utilisateur, code_hash, code_utilise, etat, ticket_hash, "
                + "destinataire, date_expiration, tentatives, utilise, date_creation) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, activation.getIdReferentiel());
            if (activation.getIdUtilisateur() == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, activation.getIdUtilisateur());
            }
            stmt.setString(3, activation.getCodeHash());
            stmt.setBoolean(4, activation.isCodeUtilise());
            stmt.setString(5, activation.getEtat());
            stmt.setString(6, activation.getTicketHash());
            stmt.setString(7, activation.getDestinataire());
            stmt.setTimestamp(8, Timestamp.valueOf(activation.getDateExpiration()));
            stmt.setInt(9, activation.getTentatives());
            stmt.setBoolean(10, activation.isUtilise());
            stmt.setTimestamp(11, Timestamp.valueOf(activation.getDateCreation()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    activation.setIdActivation(keys.getInt(1));
                }
            }
            return activation;
        }
    }

    /** Annule les demandes encore actives du même dossier (une nouvelle demande remplace l'ancienne). */
    public void annulerActivesPourReferentiel(int idReferentiel, Connection conn) throws SQLException {
        String sql = "UPDATE activation_compte SET utilise = 1, etat = 'ANNULE' "
                + "WHERE id_referentiel = ? AND utilise = 0";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReferentiel);
            stmt.executeUpdate();
        }
    }

    public Optional<ActivationCompte> findById(int idActivation) throws SQLException {
        String sql = "SELECT * FROM activation_compte WHERE id_activation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActivation);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<ActivationCompte> findDerniereActive(int idReferentiel) throws SQLException {
        String sql = "SELECT * FROM activation_compte WHERE id_referentiel = ? AND utilise = 0 "
                + "ORDER BY date_creation DESC, id_activation DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReferentiel);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<ActivationCompte> findDerniereActivePourUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM activation_compte WHERE id_utilisateur = ? AND utilise = 0 "
                + "ORDER BY date_creation DESC, id_activation DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Enregistre le jeton de définition du mot de passe et prolonge l'expiration. */
    public void enregistrerTicket(int idActivation, String ticketHash, LocalDateTime expiration,
                                  Connection conn) throws SQLException {
        String sql = "UPDATE activation_compte SET ticket_hash = ?, date_expiration = ?, "
                + "etat = 'MOT_DE_PASSE_A_DEFINIR' WHERE id_activation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ticketHash);
            stmt.setTimestamp(2, Timestamp.valueOf(expiration));
            stmt.setInt(3, idActivation);
            stmt.executeUpdate();
        }
    }

    /** Marque le code comme consommé (le ticket reste valable jusqu'à son expiration). */
    public void marquerCodeValide(int idActivation, Connection conn) throws SQLException {
        String sql = "UPDATE activation_compte SET code_utilise = 1, etat = 'CODE_VALIDE' "
                + "WHERE id_activation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idActivation);
            stmt.executeUpdate();
        }
    }

    public void marquerUtilise(int idActivation) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            marquerUtilise(idActivation, conn);
        }
    }

    public void incrementerTentatives(int idActivation) throws SQLException {
        String sql = "UPDATE activation_compte SET tentatives = tentatives + 1 WHERE id_activation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idActivation);
            stmt.executeUpdate();
        }
    }

    /** Associe le compte applicatif créé au dossier d'activation (traçabilité). */
    public void associerUtilisateur(int idActivation, int idUtilisateur, Connection conn)
            throws SQLException {
        String sql = "UPDATE activation_compte SET id_utilisateur = ? WHERE id_activation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            stmt.setInt(2, idActivation);
            stmt.executeUpdate();
        }
    }

    public void marquerUtilise(int idActivation, Connection conn) throws SQLException {
        String sql = "UPDATE activation_compte SET utilise = 1, etat = 'UTILISE' WHERE id_activation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idActivation);
            stmt.executeUpdate();
        }
    }

    /** Nombre de demandes créées pour ce dossier depuis un instant donné (limitation des envois). */
    public int compterDemandesDepuis(int idReferentiel, LocalDateTime depuis) throws SQLException {
        String sql = "SELECT COUNT(*) FROM activation_compte WHERE id_referentiel = ? AND date_creation >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReferentiel);
            stmt.setTimestamp(2, Timestamp.valueOf(depuis));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private ActivationCompte mapRow(ResultSet rs) throws SQLException {
        int utilisateur = rs.getInt("id_utilisateur");
        Integer idUtilisateur = rs.wasNull() ? null : utilisateur;

        ActivationCompte activation = new ActivationCompte(
                rs.getInt("id_activation"),
                rs.getInt("id_referentiel"),
                idUtilisateur,
                rs.getString("code_hash"),
                rs.getString("ticket_hash"),
                rs.getString("destinataire"),
                rs.getTimestamp("date_expiration").toLocalDateTime(),
                rs.getInt("tentatives"),
                rs.getBoolean("utilise"),
                rs.getTimestamp("date_creation").toLocalDateTime()
        );
        activation.setCodeUtilise(rs.getBoolean("code_utilise"));
        activation.setEtat(rs.getString("etat"));
        return activation;
    }
}
// ==================== FIN CLASSE ====================
