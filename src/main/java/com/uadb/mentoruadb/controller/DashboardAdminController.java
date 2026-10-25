package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.dto.MentorDetailVue;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/** Contrôleur lié à fxml/dashboard-admin.fxml. */
public class DashboardAdminController {

    @FXML
    private Label bienvenueLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Button deconnexionButton;
    @FXML
    private Button hamburgerButton;
    @FXML
    private VBox sidebarPane;
    @FXML
    private ToggleButton menuValidation;
    @FXML
    private ToggleButton menuMentors;
    @FXML
    private ToggleButton menuUtilisateurs;
    @FXML
    private ToggleButton menuCatalogue;
    @FXML
    private StackPane sectionsPane;
    @FXML
    private ScrollPane scrollContenu;

    // Zone de synthèse.
    @FXML
    private Label statUtilisateursValue;
    @FXML
    private Label statUtilisateursDetail;
    @FXML
    private Label statCandidaturesValue;
    @FXML
    private Label statCandidaturesDetail;
    @FXML
    private Label statMentorsValue;
    @FXML
    private Label statMentorsDetail;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final MentorDao mentorDao = new MentorDao();

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        hamburgerButton.setTooltip(new Tooltip("Masquer le menu"));
    }

    public void chargerDonnees(String prenom) {
        bienvenueLabel.setText("Administration — " + prenom);
        rafraichirStats();
    }

    /**
     * Met à jour les cartes de synthèse : elles suivent toujours le dernier
     * rafraîchissement (appelé à l'ouverture et à chaque changement de rubrique,
     * les décisions d'administration étant prises dans les rubriques).
     */
    private void rafraichirStats() {
        try {
            List<Utilisateur> utilisateurs = utilisateurDao.findAll();
            long actifs = utilisateurs.stream().filter(u -> "ACTIF".equals(u.getStatut())).count();
            statUtilisateursValue.setText(String.valueOf(utilisateurs.size()));
            statUtilisateursDetail.setText(actifs + " compte(s) actif(s)");

            List<MentorDetailVue> enAttente = mentorDao.findEnAttenteAvecDetails();
            statCandidaturesValue.setText(String.valueOf(enAttente.size()));
            statCandidaturesDetail.setText(enAttente.isEmpty()
                    ? "aucune candidature à traiter"
                    : "candidature(s) à valider");

            List<MentorDetailVue> tous = mentorDao.findTousAvecDetails();
            long valides = tous.stream().filter(m -> "VALIDE".equals(m.getStatutValidation())).count();
            statMentorsValue.setText(String.valueOf(tous.size()));
            statMentorsDetail.setText(valides + " mentor(s) validé(s)");
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des statistiques.");
        }
    }

    @FXML
    private void onHamburgerClick() {
        if (sidebarPane != null) {
            sidebarPane.setVisible(!sidebarPane.isVisible());
            sidebarPane.setManaged(sidebarPane.isVisible());
            Tooltip infobulle = hamburgerButton.getTooltip();
            if (infobulle != null) {
                infobulle.setText(sidebarPane.isVisible() ? "Masquer le menu" : "Afficher le menu");
            }
        }
    }

    /**
     * Barre latérale : affiche la section dont l'id est stocké dans le userData du
     * bouton cliqué.
     */
    @FXML
    private void onMenuClick(javafx.event.ActionEvent event) {
        ToggleButton bouton = (ToggleButton) event.getSource();
        bouton.setSelected(true); // empêche de désélectionner la rubrique courante
        afficherSection((String) bouton.getUserData());
    }

    private void afficherSection(String cible) {
        for (javafx.scene.Node section : sectionsPane.getChildren()) {
            boolean visible = cible.equals(section.getId());
            section.setVisible(visible);
            section.setManaged(visible);
        }
        scrollContenu.setVvalue(0);
        rafraichirStats();
    }

    @FXML
    private void onOuvrirValidationClick() {
        menuValidation.fire();
    }

    @FXML
    private void onOuvrirMentorsClick() {
        menuMentors.fire();
    }

    @FXML
    private void onOuvrirUtilisateursClick() {
        menuUtilisateurs.fire();
    }

    @FXML
    private void onOuvrirCatalogueClick() {
        menuCatalogue.fire();
    }

    @FXML
    private void onDeconnexionClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "Mentor-UADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }
}
// ==================== FIN CLASSE ====================
