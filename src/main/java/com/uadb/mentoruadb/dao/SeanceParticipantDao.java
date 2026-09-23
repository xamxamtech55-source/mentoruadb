package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}