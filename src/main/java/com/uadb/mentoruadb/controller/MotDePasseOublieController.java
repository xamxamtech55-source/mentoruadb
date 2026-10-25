package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.ReinitialisationService;
import com.uadb.mentoruadb.util.ChampErreur;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import com.uadb.mentoruadb.util.Validateur;
import jakarta.mail.MessagingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contrôleur lié à fxml/mot-de-passe-oublie.fxml (étape 1 : demande du code).
 * On demande l'email institutionnel ET le numéro de carte d'étudiant du compte, puis l'adresse
 * de réception du code : n'importe quel Gmail ou autre adresse existante. Ce champ est pré-rempli
 * avec l'email de récupération du compte (si renseigné) mais reste modifiable. Si l'identité
 * correspond à un compte, un code à
 * 6 chiffres est envoyé, puis l'écran reinitialisation-code.fxml prend le relais.
 * Si le compte est reconnu mais que le numéro de carte ne correspond pas, un message clair
 * s'affiche et le champ est mis en évidence. Si l'email est inconnu, le message reste neutre
 * (on ne révèle pas l'existence des comptes).
 */
public class MotDePasseOublieController {

    @FXML private TextField emailField;
    @FXML private TextField numeroCarteField;
    @FXML private TextField emailRecuperationField;
    @FXML private Label messageLabel;
    @FXML private Button envoyerCodeButton;
    @FXML private Button retourButton;

    private final ReinitialisationService reinitialisationService = new ReinitialisationService();
    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    private static final String MESSAGE_RESULTAT =
            "Si les informations correspondent à un compte, un code a été envoyé à cet email.";

    @FXML
    public void initialize() {
        // Confort : dès que l'email institutionnel est saisi, on propose l'email de récupération du compte.
        emailField.focusedProperty().addListener((obs, ancien, aLeFocus) -> {
            if (!aLeFocus) {
                prefillerEmailRecuperation();
            }
        });

        // La bordure rouge disparaît dès que l'étudiant corrige sa saisie.
        emailField.textProperty().addListener((obs, ancien, nouveau) -> ChampErreur.effacer(emailField));
        numeroCarteField.textProperty().addListener((obs, ancien, nouveau) -> ChampErreur.effacer(numeroCarteField));
        emailRecuperationField.textProperty().addListener(
                (obs, ancien, nouveau) -> ChampErreur.effacer(emailRecuperationField));

        MessageLabel.rendreDynamique(messageLabel);
    }

    /** Pré-remplit le champ de réception avec l'email de récupération du compte, sans écraser une saisie. */
    private void prefillerEmailRecuperation() {
        if (!emailRecuperationField.getText().isBlank()) {
            return;
        }
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        if (email.isEmpty()) {
            return;
        }
        try {
            utilisateurDao.findByEmail(email)
                    .map(Utilisateur::getEmailRecuperation)
                    .filter(valeur -> valeur != null && !valeur.isBlank())
                    .ifPresent(emailRecuperationField::setText);
        } catch (SQLException e) {
            // Simple confort : en cas d'erreur, l'utilisateur saisit l'adresse lui-même.
        }
    }

    @FXML
    private void onEnvoyerCodeClick() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String numeroCarte = numeroCarteField.getText() == null ? "" : numeroCarteField.getText().trim();
        String emailRecuperation = emailRecuperationField.getText() == null
                ? "" : emailRecuperationField.getText().trim();

        ChampErreur.effacer(emailField);
        ChampErreur.effacer(numeroCarteField);
        ChampErreur.effacer(emailRecuperationField);

        if (email.isBlank()) {
            ChampErreur.marquer(emailField);
            messageLabel.setText("Renseigne ton email institutionnel.");
            return;
        }
        if (!email.toLowerCase().endsWith("@uadb.edu.sn")) {
            ChampErreur.marquer(emailField);
            messageLabel.setText("Utilise ton email institutionnel (@uadb.edu.sn).");
            return;
        }
        if (numeroCarte.isBlank()) {
            ChampErreur.marquer(numeroCarteField);
            messageLabel.setText("Renseigne ton numéro de carte d'étudiant (7 chiffres).");
            return;
        }
        if (!numeroCarte.matches("\\d{7}")) {
            ChampErreur.marquer(numeroCarteField);
            messageLabel.setText("Le numéro de carte doit contenir exactement 7 chiffres.");
            return;
        }
        if (!emailRecuperation.isEmpty() && !Validateur.emailValide(emailRecuperation)) {
            ChampErreur.marquer(emailRecuperationField);
            messageLabel.setText("L'email de réception du code n'est pas valide.");
            return;
        }

        // L'envoi SMTP peut prendre quelques secondes : on le fait hors du thread JavaFX.
        envoyerCodeButton.setDisable(true);
        messageLabel.setText("Envoi du code en cours…");

        new Thread(() -> {
            try {
                Optional<String> destination = reinitialisationService.envoyerCode(email, numeroCarte, emailRecuperation);
                Platform.runLater(() -> {
                    envoyerCodeButton.setDisable(false);
                    if (destination.isPresent()) {
                        ouvrirEcranCode(email, destination.get());
                    } else {
                        messageLabel.setText(MESSAGE_RESULTAT);
                    }
                });
            } catch (IllegalArgumentException e) {
                // Compte reconnu mais numéro de carte incorrect : message clair + champ en rouge.
                Platform.runLater(() -> {
                    envoyerCodeButton.setDisable(false);
                    ChampErreur.marquer(numeroCarteField);
                    messageLabel.setText(e.getMessage());
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

    private void ouvrirEcranCode(String emailInstitutionnel, String emailRecuperation) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            ReinitialisationCodeController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/reinitialisation-code.fxml",
                    "Mentor-UADB - Code de réinitialisation"
            );
            controller.setContexte(emailInstitutionnel, emailRecuperation);
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
// ==================== FIN CLASSE ====================
