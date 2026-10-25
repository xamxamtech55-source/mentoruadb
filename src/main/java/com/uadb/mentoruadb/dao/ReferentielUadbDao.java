package com.uadb.mentoruadb.dao;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.config.ReferentielConnection;
import com.uadb.mentoruadb.dto.EtudiantReferentielExterne;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
public class ReferentielUadbDao {

    private static final String COLONNES = """
            SELECT id, numero_carte, email_institutionnel, email_personnel,
                   prenom, nom, filiere_code, niveau_code, statut_inscription
            FROM etudiants
            """;

    public Optional<EtudiantReferentielExterne> findByCarteEtEmail(String numeroCarte, String email)
            throws SQLException {
        String sql = COLONNES + " WHERE numero_carte = ? AND LOWER(email_institutionnel) = ? LIMIT 1";

        try (Connection conn = ReferentielConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCarte);
            stmt.setString(2, email == null ? "" : email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public Optional<EtudiantReferentielExterne> findByCarte(String numeroCarte) throws SQLException {
        String sql = COLONNES + " WHERE numero_carte = ? LIMIT 1";

        try (Connection conn = ReferentielConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroCarte);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    private EtudiantReferentielExterne mapRow(ResultSet rs) throws SQLException {
        return new EtudiantReferentielExterne(
                rs.getLong("id"),
                rs.getString("numero_carte"),
                rs.getString("email_institutionnel"),
                rs.getString("email_personnel"),
                rs.getString("prenom"),
                rs.getString("nom"),
                rs.getString("filiere_code"),
                rs.getString("niveau_code"),
                rs.getString("statut_inscription")
        );
    }
}
// ==================== FIN CLASSE ====================
