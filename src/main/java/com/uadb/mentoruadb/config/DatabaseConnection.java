package com.uadb.mentoruadb.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fournit une connexion JDBC unique vers la base MySQL "mentoruadb".
 * Adapter URL / utilisateur / mot de passe à votre environnement local.
 *
 * Membre 2 (Base de données / JDBC) : c'est le fichier à faire évoluer
 * en priorité (pool de connexions si besoin, gestion des erreurs, etc.)
 */
public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/mentoruadb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private DatabaseConnection() {
        // utilitaire : pas d'instanciation
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Petit test manuel : exécuter cette méthode pour vérifier que la
     * connexion JDBC/MySQL fonctionne avant de commencer le développement.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie à MySQL : " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Échec de connexion à MySQL : " + e.getMessage());
        }
    }
}
