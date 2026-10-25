package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.AuthService;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import com.uadb.mentoruadb.util.Validateur;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contrôleur lié à fxml/login-admin.fxml : espace de connexion réservé aux
 * administrateurs. Séparé du formulaire étudiant/mentor (pas de numéro de
 * carte) et refuse tout compte qui n'a pas le rôle ADMIN.
 */
public class LoginAdminController {

    /** En dessous de cette largeur, le bloc institutionnel est masqué. */
    private static final double SEUIL_COMPACT = 760.0;

    @FXML
    private ScrollPane scrollRacine;
    @FXML
    private StackPane racine;
    @FXML
    private VBox blocInstitutionnel;
    @FXML
    private VBox carteConnexion;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField motDePasseField;
    @FXML
    private TextField motDePasseVisibleField;
    @FXML
    private Button toggleMotDePasseButton;
    @FXML
    private Button connexionButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Button retourButton;

    private final AuthService authService = new AuthService();

    private boolean motDePasseVisible = false;

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());

        emailField.setOnAction(e -> onConnexionClick());
        motDePasseField.setOnAction(e -> onConnexionClick());

        racine.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> scrollRacine.getViewportBounds().getHeight(), scrollRacine.viewportBoundsProperty()));

        racine.widthProperty().addListener((obs, ancienne, nouvelle) -> adapterMiseEnPage(nouvelle.doubleValue()));
        adapterMiseEnPage(racine.getWidth());

        apparitionCarte();
        Platform.runLater(emailField::requestFocus);
    }

    /** Masque le bloc institutionnel en fenêtre étroite : la carte occupe alors tout l'espace. */
    private void adapterMiseEnPage(double largeur) {
        boolean compact = largeur < SEUIL_COMPACT;
        blocInstitutionnel.setVisible(!compact);
        blocInstitutionnel.setManaged(!compact);
    }

    /** Apparition discrète de la carte (une seule fois, sans boucle). */
    private void apparitionCarte() {
        carteConnexion.setOpacity(0.0);
        carteConnexion.setTranslateY(10.0);

        FadeTransition fondu = new FadeTransition(Duration.millis(260), carteConnexion);
        fondu.setFromValue(0.0);
        fondu.setToValue(1.0);
        fondu.setDelay(Duration.millis(40));

        TranslateTransition levee = new TranslateTransition(Duration.millis(260), carteConnexion);
        levee.setFromY(10.0);
        levee.setToY(0.0);
        levee.setDelay(Duration.millis(40));

        fondu.play();
        levee.play();
    }

    @FXML
    private void onToggleMotDePasseClick() {
        motDePasseVisible = !motDePasseVisible;
        motDePasseField.setVisible(!motDePasseVisible);
        motDePasseField.setManaged(!motDePasseVisible);
        motDePasseVisibleField.setVisible(motDePasseVisible);
        motDePasseVisibleField.setManaged(motDePasseVisible);
        toggleMotDePasseButton.setText(motDePasseVisible ? "Masquer" : "Afficher");
    }

    @FXML
    private void onConnexionClick() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String motDePasse = motDePasseField.getText() == null ? "" : motDePasseField.getText();

        if (email.isBlank() || motDePasse.isBlank()) {
            messageLabel.setText("Renseigne l'email et le mot de passe.");
            return;
        }
        if (!Validateur.emailValide(email)) {
            messageLabel.setText("L'adresse email est invalide.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.connecterAdministrateur(email, motDePasse);
            if (utilisateur == null) {
                messageLabel.setText("Email ou mot de passe incorrect.");
                return;
            }

            if (utilisateur.isMustChangePassword()) {
                ouvrirChangementObligatoire(utilisateur);
                return;
            }

            SessionNavigation.ouvrirEspace((Stage) racine.getScene().getWindow(), utilisateur);

        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données.");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'administration.");
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void ouvrirChangementObligatoire(Utilisateur utilisateur) throws IOException {
        Stage stage = (Stage) racine.getScene().getWindow();
        ChangementMotDePasseObligatoireController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/changement-mot-de-passe-obligatoire.fxml",
                "Mentor-UADB - Nouveau mot de passe");
        controller.initialiser(utilisateur);
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml",
                    "Mentor-UADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de connexion.");
        }
    }
}
// ==================== FIN CLASSE ====================
