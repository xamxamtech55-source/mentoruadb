package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/** Contrôleur lié à fxml/bienvenue.fxml. Écran d'accueil affiché au démarrage, avant la connexion. */
public class BienvenueController {

    private static final String CHEMIN_CONNEXION = "/com/uadb/mentoruadb/fxml/login.fxml";
    private static final String CHEMIN_INSCRIPTION = "/com/uadb/mentoruadb/fxml/inscription.fxml";

    @FXML private VBox contenu;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        animerEntree();
    }

    /** Fondu + léger glissement du bloc central à l'affichage, pour une arrivée plus soignée. */
    private void animerEntree() {
        FadeTransition fondu = new FadeTransition(Duration.millis(800), contenu);
        fondu.setFromValue(0.0);
        fondu.setToValue(1.0);

        TranslateTransition glissement = new TranslateTransition(Duration.millis(800), contenu);
        glissement.setFromY(26.0);
        glissement.setToY(0.0);
        glissement.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(fondu, glissement).play();
    }

    @FXML
    private void onConnexionClick() {
        naviguer(CHEMIN_CONNEXION, "Mentor-UADB - Connexion");
    }

    @FXML
    private void onInscriptionClick() {
        naviguer(CHEMIN_INSCRIPTION, "Mentor-UADB - Inscription");
    }

    @FXML
    private void onCarteTrouverMentorClick(MouseEvent event) {
        naviguer(CHEMIN_CONNEXION, "Mentor-UADB - Connexion");
    }

    @FXML
    private void onCarteDevenirMentorClick(MouseEvent event) {
        naviguer(CHEMIN_INSCRIPTION, "Mentor-UADB - Inscription");
    }

    @FXML
    private void onCarteSuiviMentoratClick(MouseEvent event) {
        naviguer(CHEMIN_CONNEXION, "Mentor-UADB - Connexion");
    }

    private void naviguer(String fxmlPath, String titre) {
        try {
            Stage stage = (Stage) contenu.getScene().getWindow();
            SceneNavigator.switchTo(stage, fxmlPath, titre);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}
