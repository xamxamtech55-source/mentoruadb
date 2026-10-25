package com.uadb.mentoruadb.config;

// ==================== DÉBUT IMPORTS ====================
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
public final class ReferentielConnection {

    private static final String URL_PAR_DEFAUT =
            "jdbc:mysql://localhost:3306/referentiel_uadb?useSSL=false&serverTimezone=UTC";
    private static final String UTILISATEUR_PAR_DEFAUT = "root";
    private static final String MOT_DE_PASSE_PAR_DEFAUT = "";

    private static final String FICHIER_CONFIG = "db.properties";

    private static final String url;
    private static final String utilisateur;
    private static final String motDePasse;

    static {
        Properties fichier = chargerFichier();
        url = choisir("MENTORUADB_REF_URL", fichier.getProperty("ref.url"), URL_PAR_DEFAUT);
        utilisateur = choisir("MENTORUADB_REF_USER", fichier.getProperty("ref.user"), UTILISATEUR_PAR_DEFAUT);
        motDePasse = choisir("MENTORUADB_REF_PASSWORD", fichier.getProperty("ref.password"), MOT_DE_PASSE_PAR_DEFAUT);
    }

    private ReferentielConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, utilisateur, motDePasse);
    }

    public static String getUrl() {
        return url;
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

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie au référentiel : " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Échec de connexion au référentiel : " + e.getMessage());
        }
    }
}
// ==================== FIN CLASSE ====================
