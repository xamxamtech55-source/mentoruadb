package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur lié à fxml/bienvenue.fxml. Écran d'accueil affiché au démarrage,
 * avant la connexion.
 */
public class BienvenueController {

    private static final String CHEMIN_CONNEXION = "/com/uadb/mentoruadb/fxml/login.fxml";
    private static final String CHEMIN_INSCRIPTION = "/com/uadb/mentoruadb/fxml/inscription.fxml";

    /** Sous cette largeur, la barre latérale laisse place à la barre compacte. */
    private static final double SEUIL_COMPACT = 760.0;
    /** Sous cette largeur, la photo de la bannière est masquée. */
    private static final double SEUIL_PHOTO = 900.0;

    @FXML
    private HBox racine;
    @FXML
    private VBox sidebar;
    @FXML
    private HBox barreCompacte;
    @FXML
    private ImageView imageHero;
    @FXML
    private Button menuAccueilButton;
    @FXML
    private Button menuConnexionButton;
    @FXML
    private Button menuAProposButton;
    @FXML
    private Button menuAideButton;
    @FXML
    private Button connexionHeroButton;
    @FXML
    private Button inscriptionHeroButton;
    @FXML
    private VBox carte1;
    @FXML
    private VBox carte2;
    @FXML
    private VBox carte3;
    @FXML
    private VBox carte4;

    @FXML
    public void initialize() {
        racine.widthProperty().addListener((obs, ancienne, nouvelle) -> adapterMiseEnPage(nouvelle.doubleValue()));
        adapterMiseEnPage(racine.getWidth());
        apparitionCartes(List.of(carte1, carte2, carte3, carte4));
    }

    /** Adapte la navigation et la bannière à la largeur de la fenêtre. */
    private void adapterMiseEnPage(double largeur) {
        boolean compact = largeur < SEUIL_COMPACT;
        sidebar.setVisible(!compact);
        sidebar.setManaged(!compact);
        barreCompacte.setVisible(compact);
        barreCompacte.setManaged(compact);

        boolean photo = largeur >= SEUIL_PHOTO;
        imageHero.setVisible(photo);
        imageHero.setManaged(photo);
    }

    /** Arrivée progressive des cartes, une à une. */
    private void apparitionCartes(List<VBox> cartes) {
        for (int i = 0; i < cartes.size(); i++) {
            VBox carte = cartes.get(i);
            carte.setOpacity(0.0);

            FadeTransition apparition = new FadeTransition(Duration.millis(520), carte);
            apparition.setFromValue(0.0);
            apparition.setToValue(1.0);
            apparition.setDelay(Duration.millis(150L + i * 150L));
            apparition.play();
        }
    }

    @FXML
    private void onMenuAccueilClick() {
        // Déjà sur l'accueil : rien à faire.
    }

    @FXML
    private void onMenuConnexionClick() {
        naviguer(CHEMIN_CONNEXION, "Mentor-UADB - Connexion");
    }

    @FXML
    private void onMenuAProposClick() {
        afficherInfo("À propos",
                "MentorUADB v1.0\n\nPlateforme de mentorat de l'Université Alioune Diop de Bambey (UADB), "
                        + "qui rapproche les étudiants de mentors qualifiés.");
    }

    @FXML
    private void onMenuAideClick() {
        afficherInfo("Aide",
                "Pour vous connecter, utilisez votre email institutionnel (@uadb.edu.sn) et votre mot de passe.\n\n"
                        + "Si vous avez oublié votre mot de passe, utilisez le lien « Mot de passe oublié ? » "
                        + "sur l'écran de connexion.");
    }

    @FXML
    private void onConnexionClick() {
        naviguer(CHEMIN_CONNEXION, "Mentor-UADB - Connexion");
    }

    @FXML
    private void onInscriptionClick() {
        naviguer(CHEMIN_INSCRIPTION, "Mentor-UADB - Inscription");
    }

    private void naviguer(String fxmlPath, String titre) {
        try {
            Stage stage = (Stage) racine.getScene().getWindow();
            SceneNavigator.switchTo(stage, fxmlPath, titre);
        } catch (IOException e) {
            afficherInfo("Erreur", "Impossible d'ouvrir cet écran.");
        }
    }

    private void afficherInfo(String titre, String contenu) {
        Alert alerte = new Alert(Alert.AlertType.INFORMATION);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(contenu);
        alerte.showAndWait();
    }
}