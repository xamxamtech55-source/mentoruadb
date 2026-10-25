package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Ouverture de l'espace correspondant au rôle du compte connecté.
 * Utilisé après une connexion classique, après le changement obligatoire du
 * mot de passe et à la fin de la première connexion.
 */
public final class SessionNavigation {

    private static final EtudiantDao ETUDIANT_DAO = new EtudiantDao();

    private SessionNavigation() {
    }

    public static void ouvrirEspace(Stage stage, Utilisateur utilisateur)
            throws SQLException, IOException {
        switch (utilisateur.getRole()) {
            case "ETUDIANT", "MENTOR" -> ouvrirEspaceEtudiant(stage, utilisateur);
            case "ADMIN" -> ouvrirEspaceAdmin(stage, utilisateur);
            default -> throw new IllegalStateException("Rôle non reconnu : " + utilisateur.getRole());
        }
    }

    private static void ouvrirEspaceEtudiant(Stage stage, Utilisateur utilisateur)
            throws SQLException, IOException {
        Optional<Etudiant> etudiant = ETUDIANT_DAO.findByUtilisateur(utilisateur.getIdUtilisateur());
        if (etudiant.isEmpty()) {
            throw new IllegalStateException("Profil étudiant introuvable pour ce compte.");
        }
        DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml",
                "Mentor-UADB - Tableau de bord");
        controller.chargerDonnees(etudiant.get(), utilisateur.getPrenom());
    }

    private static void ouvrirEspaceAdmin(Stage stage, Utilisateur utilisateur) throws IOException {
        DashboardAdminController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml",
                "Mentor-UADB - Administration");
        controller.chargerDonnees(utilisateur.getPrenom());
    }
}
// ==================== FIN CLASSE ====================
