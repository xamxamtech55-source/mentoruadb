package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.service.ReinitialisationService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur lié à fxml/reinitialisation-code.fxml (étape 2 : saisie du code reçu par mail).
 * L'email est transmis par MotDePasseOublieController via setEmail().
 */
public class ReinitialisationCodeController {

    @FXML private Label emailEnvoyeLabel;
    @FXML private TextField codeField;
    @FXML private PasswordField nouveauMotDePasseField;
    @FXML private PasswordField confirmationField;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final ReinitialisationService reinitialisationService = new ReinitialisationService();

    private String email;

    /** Appelé par l'écran précédent juste après le chargement de cette scène. */
    public void setEmail(String email) {
        this.email = email;
        emailEnvoyeLabel.setText("Code envoyé à " + email);
    }

    @FXML
    private void onVerifierClick() {
        if (email == null) {
            messageLabel.setText("Aucun email en cours de réinitialisation. Refais une demande.");
            return;
        }

        try {
            reinitialisationService.reinitialiser(
                    email,
                    codeField.getText(),
                    nouveauMotDePasseField.getText(),
                    confirmationField.getText()
            );
            messageLabel.setText("Mot de passe réinitialisé avec succès. Tu peux te connecter.");
            codeField.clear();
            nouveauMotDePasseField.clear();
            confirmationField.clear();
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la réinitialisation.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "Mentor-UADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }
}
