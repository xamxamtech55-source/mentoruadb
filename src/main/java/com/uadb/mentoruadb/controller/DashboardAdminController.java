package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/** Contrôleur lié à fxml/dashboard-admin.fxml. */
public class DashboardAdminController {

    @FXML private Label bienvenueLabel;
    @FXML private Label messageLabel;
    @FXML private Button deconnexionButton;

    public void chargerDonnees(String prenom) {
        bienvenueLabel.setText("Administration — " + prenom);
    }

    @FXML
    private void onValiderMentorsClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/validation-mentors.fxml", "MentorUADB - Validation des mentors");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de validation.");
        }
    }

    @FXML
    private void onGestionDonneesClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/gestion-donnees.fxml", "MentorUADB - Gestion des données");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }

    @FXML
    private void onDeconnexionClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }
    @FXML
    private void onGestionUtilisateursClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/gestion-utilisateurs.fxml", "MentorUADB - Gestion des utilisateurs");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}