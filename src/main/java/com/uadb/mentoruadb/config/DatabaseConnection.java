package com.uadb.mentoruadb.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fournit une connexion JDBC vers la base MySQL "mentoruadb".
 *
 * Configuration (par ordre de priorité) :
 *   1. variables d'environnement MENTORUADB_DB_URL, MENTORUADB_DB_USER, MENTORUADB_DB_PASSWORD
 *   2. fichier db.properties à la racine du projet (clés db.url, db.user, db.password) — voir db.properties.example,
 *      ce fichier est ignoré par Git : il ne doit jamais être commité
 *   3. valeurs par défaut pour un MySQL local de développement (root, sans mot de passe)
 *
 * Chaque appel à getConnection() renvoie une NOUVELLE connexion. L'appelant la ferme (try-with-resources,
 * comme le font déjà tous les DAO). Il n'y a donc plus de connexion partagée entre les écrans.
 */
public final class DatabaseConnection {

    private static final String URL_PAR_DEFAUT =
            "jdbc:mysql://localhost:3306/mentoruadb?useSSL=false&serverTimezone=UTC";
    private static final String UTILISATEUR_PAR_DEFAUT = "root";
    private static final String MOT_DE_PASSE_PAR_DEFAUT = "";

    private static final String FICHIER_CONFIG = "db.properties";

    private static final String url;
    private static final String utilisateur;
    private static final String motDePasse;

    static {
        Properties fichier = chargerFichier();
        url = choisir("MENTORUADB_DB_URL", fichier.getProperty("db.url"), URL_PAR_DEFAUT);
        utilisateur = choisir("MENTORUADB_DB_USER", fichier.getProperty("db.user"), UTILISATEUR_PAR_DEFAUT);
        motDePasse = choisir("MENTORUADB_DB_PASSWORD", fichier.getProperty("db.password"), MOT_DE_PASSE_PAR_DEFAUT);
    }

    private DatabaseConnection() {
        // utilitaire : pas d'instanciation
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, utilisateur, motDePasse);
    }

    private static String choisir(String variableEnv, String valeurFichier, String valeurParDefaut) {
        String env = System.getenv(variableEnv);
        if (env != null && !env.isEmpty()) {
            return env;
        }
        if (valeurFichier != null) {
            return valeurFichier;
        }
        return valeurParDefaut;
    }

    private static Properties chargerFichier() {
        Properties proprietes = new Properties();
        Path chemin = Path.of(FICHIER_CONFIG);
        if (Files.isRegularFile(chemin)) {
            try (InputStream in = Files.newInputStream(chemin)) {
                proprietes.load(in);
            } catch (IOException e) {
                System.err.println("Impossible de lire " + FICHIER_CONFIG + " : " + e.getMessage());
            }
        }
        return proprietes;
    }

    /**
     * Petit test manuel : exécuter cette méthode pour vérifier que la
     * connexion JDBC/MySQL fonctionne avant de commencer le développement.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie à MySQL : " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Échec de connexion : " + e.getMessage());
        }
    }
}
