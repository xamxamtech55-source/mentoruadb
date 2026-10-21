package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

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
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        hamburgerButton.setTooltip(new Tooltip("Masquer le menu"));
    }

    public void chargerDonnees(String prenom) {
        bienvenueLabel.setText("Administration — " + prenom);
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
