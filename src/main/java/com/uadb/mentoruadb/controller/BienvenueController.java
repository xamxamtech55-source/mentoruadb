package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/** Contrôleur lié à fxml/bienvenue.fxml. Écran d'accueil affiché au démarrage, avant la connexion. */
public class BienvenueController {

    private static final String CHEMIN_CONNEXION = "/com/uadb/mentoruadb/fxml/login.fxml";
    private static final String CHEMIN_INSCRIPTION = "/com/uadb/mentoruadb/fxml/inscription.fxml";

    @FXML private ImageView imageFond;
    @FXML private VBox contenu;
    @FXML private VBox entete;
    @FXML private VBox carteTrouverMentor;
    @FXML private VBox carteDevenirMentor;
    @FXML private VBox carteSuiviMentorat;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        animerFond();
        animerEntree();
    }

    /**
     * Effet « Ken Burns » : la photo des étudiants à la bibliothèque zoome et se déplace lentement
     * en boucle, comme un héros d'application web.
     */
    private void animerFond() {
        Duration duree = Duration.seconds(26);

        ScaleTransition zoom = new ScaleTransition(duree, imageFond);
        zoom.setFromX(1.0);
        zoom.setFromY(1.0);
        zoom.setToX(1.12);
        zoom.setToY(1.12);

        TranslateTransition panoramique = new TranslateTransition(duree, imageFond);
        panoramique.setFromX(0.0);
        panoramique.setToX(-30.0);
        panoramique.setFromY(0.0);
        panoramique.setToY(-16.0);

        ParallelTransition fond = new ParallelTransition(zoom, panoramique);
        fond.setInterpolator(Interpolator.EASE_BOTH);
        fond.setCycleCount(Animation.INDEFINITE);
        fond.setAutoReverse(true);
        fond.play();
    }

    /** Arrivée progressive des éléments : le bloc central glisse en fondu, puis les cartes apparaissent une à une. */
    private void animerEntree() {
        FadeTransition fondu = new FadeTransition(Duration.millis(700), contenu);
        fondu.setFromValue(0.0);
        fondu.setToValue(1.0);

        TranslateTransition glissement = new TranslateTransition(Duration.millis(700), contenu);
        glissement.setFromY(28.0);
        glissement.setToY(0.0);
        glissement.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(fondu, glissement).play();

        List<VBox> cartes = List.of(carteTrouverMentor, carteDevenirMentor, carteSuiviMentorat);
        for (int i = 0; i < cartes.size(); i++) {
            VBox carte = cartes.get(i);
            carte.setOpacity(0.0);

            FadeTransition apparition = new FadeTransition(Duration.millis(520), carte);
            apparition.setFromValue(0.0);
            apparition.setToValue(1.0);
            apparition.setDelay(Duration.millis(240L + i * 150L));
            apparition.play();
        }
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
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}
