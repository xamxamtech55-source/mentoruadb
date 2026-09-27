package com.uadb.mentoruadb.dao;

import com.uadb.mentoruadb.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Table d'association pure (clé composite id_filiere + id_matiere) -> pas d'interface Dao<T,ID>. */
public class FiliereMatiereDao {

    public void ajouter(int idFiliere, int idMatiere) throws SQLException {
        String sql = "INSERT IGNORE INTO filiere_matiere (id_filiere, id_matiere) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            stmt.setInt(2, idMatiere);
            stmt.executeUpdate();
        }
    }

    public void retirer(int idFiliere, int idMatiere) throws SQLException {
        String sql = "DELETE FROM filiere_matiere WHERE id_filiere = ? AND id_matiere = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFiliere);
            stmt.setInt(2, idMatiere);
            stmt.executeUpdate();
        }
    }
}