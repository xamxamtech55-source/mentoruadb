package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.service.ReinitialisationService;
import com.uadb.mentoruadb.util.SceneNavigator;
import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur lié à fxml/mot-de-passe-oublie.fxml (étape 1 : demande du code).
 * On demande l'email institutionnel ET le numéro de carte d'étudiant (les deux sont créés à
 * l'inscription, voir InscriptionController) : si l'identité correspond à un compte, un code
 * à 6 chiffres est envoyé par mail, puis l'écran reinitialisation-code.fxml prend le relais.
 * Le message est le même que l'identité corresponde ou non : ne rien révéler sur les
 * comptes existants (énumération d'email).
 */
public class MotDePasseOublieController {

    @FXML private TextField emailField;
    @FXML private TextField numeroCarteField;
    @FXML private Label messageLabel;
    @FXML private Button envoyerCodeButton;
    @FXML private Button retourButton;

    private final ReinitialisationService reinitialisationService = new ReinitialisationService();

    private static final String MESSAGE_RESULTAT =
            "Si les informations correspondent à un compte, un code a été envoyé à cet email.";

    @FXML
    private void onEnvoyerCodeClick() {
        String email = emailField.getText().trim();
        String numeroCarte = numeroCarteField.getText().trim();

        if (email.isBlank() || numeroCarte.isBlank()) {
            messageLabel.setText("Remplis tous les champs.");
            return;
        }
        if (!email.toLowerCase().endsWith("@uadb.edu.sn")) {
            messageLabel.setText("Utilise ton email institutionnel (@uadb.edu.sn).");
            return;
        }

        // L'envoi SMTP peut prendre quelques secondes : on le fait hors du thread JavaFX.
        envoyerCodeButton.setDisable(true);
        messageLabel.setText("Envoi du code en cours…");

        new Thread(() -> {
            try {
                boolean envoye = reinitialisationService.envoyerCode(email, numeroCarte);
                Platform.runLater(() -> {
                    envoyerCodeButton.setDisable(false);
                    if (envoye) {
                        ouvrirEcranCode(email);
                    } else {
                        messageLabel.setText(MESSAGE_RESULTAT);
                    }
                });
            } catch (MessagingException e) {
                Platform.runLater(() -> {
                    envoyerCodeButton.setDisable(false);
                    messageLabel.setText("Impossible d'envoyer le mail de réinitialisation : " + e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    envoyerCodeButton.setDisable(false);
                    messageLabel.setText("Erreur lors de la demande de réinitialisation.");
                });
            }
        }, "envoi-code-reinitialisation").start();
    }

    private void ouvrirEcranCode(String email) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            ReinitialisationCodeController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/reinitialisation-code.fxml",
                    "Mentor-UADB - Code de réinitialisation"
            );
            controller.setEmail(email);
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de saisie du code.");
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
