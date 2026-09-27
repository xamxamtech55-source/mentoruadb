package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Table d'association pure (clé composite id_seance + id_etudiant) -> pas d'interface Dao<T,ID>. */
public class SeanceParticipantDao {

    public void ajouter(int idSeance, int idEtudiant) throws SQLException {
        String sql = "INSERT INTO seance_participant (id_seance, id_etudiant) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            stmt.setInt(2, idEtudiant);
            stmt.executeUpdate();
        }
    }

    public void confirmerPresence(int idSeance, int idEtudiant) throws SQLException {
        String sql = "UPDATE seance_participant SET confirme = 1 WHERE id_seance = ? AND id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            stmt.setInt(2, idEtudiant);
            stmt.executeUpdate();
        }
    }

    public void marquerVu(int idSeance, int idEtudiant) throws SQLException {
        String sql = "UPDATE seance_participant SET vu = 1 WHERE id_seance = ? AND id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            stmt.setInt(2, idEtudiant);
            stmt.executeUpdate();
        }
    }

    /** L'étudiant masque cette séance de groupe de sa propre vue (les autres participants ne sont pas affectés). */
    public void masquerPourEtudiant(int idSeance, int idEtudiant) throws SQLException {
        String sql = "UPDATE seance_participant SET masque = 1 WHERE id_seance = ? AND id_etudiant = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            stmt.setInt(2, idEtudiant);
            stmt.executeUpdate();
        }
    }

    public List<String> findNomsConfirmes(int idSeance) throws SQLException {
        String sql = """
                SELECT u.prenom, u.nom
                FROM seance_participant sp
                JOIN etudiant e ON sp.id_etudiant = e.id_etudiant
                JOIN utilisateur u ON e.id_utilisateur = u.id_utilisateur
                WHERE sp.id_seance = ? AND sp.confirme = 1
                ORDER BY u.nom
                """;

        List<String> resultats = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(rs.getString("prenom") + " " + rs.getString("nom"));
                }
            }
        }
        return resultats;
    }

    public int compterParticipants(int idSeance) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seance_participant WHERE id_seance = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            try (var rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int compterConfirmes(int idSeance) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seance_participant WHERE id_seance = ? AND confirme = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSeance);
            try (var rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}