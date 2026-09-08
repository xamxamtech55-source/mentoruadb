package com.uadb.mentoruadb.service;

import com.uadb.mentoruadb.config.DatabaseConnection;
import com.uadb.mentoruadb.model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service d'authentification.
 * Membre 4 (Logique métier / Controllers) : à compléter avec le hachage
 * du mot de passe (ex. BCrypt) avant la mise en production réelle.
 */
public class AuthService {

    public Utilisateur connecter(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND mot_de_passe = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, motDePasse);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                            rs.getInt("id_utilisateur"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            rs.getString("mot_de_passe"),
                            rs.getString("role"),
                            rs.getString("statut")
                    );
                }
                return null; // identifiants incorrects
            }
        }
    }
}
