package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Notifications génériques adressées au compte utilisateur d'un étudiant ou d'un mentor.
 * creerPourEtudiant / creerPourMentor résolvent directement en SQL (id_etudiant -> id_utilisateur,
 * id_mentor -> id_etudiant -> id_utilisateur) : les appelants n'ont besoin que de l'id métier
 * qu'ils ont déjà sous la main (pas besoin d'aller chercher l'id_utilisateur séparément).
 */
public class NotificationDao {

    /** Notifie le compte d'un étudiant, en résolvant id_etudiant -> id_utilisateur. */
    public void creerPourEtudiant(int idEtudiant, String message) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            creerPourEtudiant(idEtudiant, message, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public void creerPourEtudiant(int idEtudiant, String message, Connection conn) throws SQLException {
        String sql = "INSERT INTO notification (id_utilisateur, message, lue, date_creation) " +
                "SELECT e.id_utilisateur, ?, 0, ? FROM etudiant e WHERE e.id_etudiant = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idEtudiant);
            stmt.executeUpdate();
        }
    }

    /** Notifie le compte d'un mentor, en résolvant id_mentor -> id_etudiant -> id_utilisateur. */
    public void creerPourMentor(int idMentor, String message) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            creerPourMentor(idMentor, message, conn);
        }
    }

    /** Variante transactionnelle : utilise la connexion fournie et NE la ferme PAS (c'est à l'appelant de le faire). */
    public void creerPourMentor(int idMentor, String message, Connection conn) throws SQLException {
        String sql = "INSERT INTO notification (id_utilisateur, message, lue, date_creation) " +
                "SELECT e.id_utilisateur, ?, 0, ? FROM mentor m JOIN etudiant e ON e.id_etudiant = m.id_etudiant " +
                "WHERE m.id_mentor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idMentor);
            stmt.executeUpdate();
        }
    }

    /** Notifications non lues d'un compte utilisateur, les plus récentes en premier. */
    public List<Notification> findNonLuesByUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id_utilisateur = ? AND lue = 0 ORDER BY date_creation DESC";
        List<Notification> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
        }
        return resultats;
    }

    /** Marque toutes les notifications non lues d'un utilisateur comme lues (appelé quand il les consulte). */
    public void marquerToutesLuesPourUtilisateur(int idUtilisateur) throws SQLException {
        String sql = "UPDATE notification SET lue = 1 WHERE id_utilisateur = ? AND lue = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            stmt.executeUpdate();
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("id_notification"),
                rs.getInt("id_utilisateur"),
                rs.getString("message"),
                rs.getBoolean("lue"),
                rs.getTimestamp("date_creation").toLocalDateTime()
        );
    }
}