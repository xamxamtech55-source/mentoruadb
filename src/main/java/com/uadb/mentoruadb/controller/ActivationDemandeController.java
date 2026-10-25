package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.service.ActivationService;
import com.uadb.mentoruadb.util.ChampErreur;
import com.uadb.mentoruadb.util.ChampTelephone;
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
// ==================== FIN IMPORTS ====================

    // ==================== DÉBUT CLASSE ====================
    /**
     * Étape 1 de la première connexion : l'étudiant saisit son numéro de carte,
     * son email institutionnel et une adresse e-mail personnelle de son choix.
     * Seuls la carte et l'email institutionnel doivent correspondre au dossier
     * du référentiel ; l'email personnel n'est pas comparé au référentiel et est
     * celui vers lequel le code est envoyé.
     */
    public class ActivationDemandeController {

    @FXML private TextField numeroCarteField;
    @FXML private TextField emailField;
    @FXML private TextField emailPersonnelField;
    @FXML private Label messageLabel;
    @FXML private Button envoyerButton;
    @FXML private Button retourButton;

    private final ActivationService activationService = new ActivationService();

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        ChampTelephone.chiffres(numeroCarteField, 7);
        numeroCarteField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(numeroCarteField));
        emailField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(emailField));
        emailPersonnelField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(emailPersonnelField));
    }

    @FXML
    private void onEnvoyerClick() {
        String carte = numeroCarteField.getText() == null ? "" : numeroCarteField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String personnel = emailPersonnelField.getText() == null
                ? "" : emailPersonnelField.getText().trim();

        ChampErreur.effacer(numeroCarteField);
        ChampErreur.effacer(emailField);
        ChampErreur.effacer(emailPersonnelField);

        if (!Validateur.numeroCarteValide(carte)) {
            ChampErreur.marquer(numeroCarteField);
            messageLabel.setText("Le numéro de carte doit contenir exactement 7 chiffres.");
            return;
        }
        if (!Validateur.emailValide(email) || !email.toLowerCase().endsWith("@uadb.edu.sn")) {
            ChampErreur.marquer(emailField);
            messageLabel.setText("Utilise ton email institutionnel (@uadb.edu.sn).");
            return;
        }
        if (!Validateur.emailValide(personnel)) {
            ChampErreur.marquer(emailPersonnelField);
            messageLabel.setText("Renseigne une adresse email personnelle valide pour recevoir le code de validation.");
            return;
        }

        envoyerButton.setDisable(true);
        messageLabel.setText("Envoi du code en cours…");

        new Thread(() -> {
            try {
                ActivationService.DemandeResultat resultat =
                        activationService.demanderCode(carte, email, personnel);
                Platform.runLater(() -> {
                    envoyerButton.setDisable(false);
                    traiterResultat(resultat, carte, email, personnel);
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    envoyerButton.setDisable(false);
                    messageLabel.setText(e.getMessage());
                });
            } catch (IllegalStateException | MessagingException e) {
                Platform.runLater(() -> {
                    envoyerButton.setDisable(false);
                    messageLabel.setText(e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    envoyerButton.setDisable(false);
                    messageLabel.setText("Erreur lors de la demande de première connexion.");
                });
            }
        }, "activation-demande").start();
    }

    private void traiterResultat(ActivationService.DemandeResultat resultat,
                                 String carte, String email, String personnel) {
        switch (resultat.statut()) {
            case ENVOYE -> ouvrirSaisieCode(carte, email, personnel);
            case INFORMATIONS_INCORRECTES -> messageLabel.setText(
                    "Les informations ne correspondent pas au dossier universitaire. "
                            + "Vérifie ton numéro de carte et ton email institutionnel.");
            case INSCRIPTION_INACTIVE -> messageLabel.setText(
                    "Ton inscription universitaire n'est pas active. Contacte le service de scolarité.");
            case COMPTE_EXISTANT -> messageLabel.setText(
                    "Un compte existe déjà pour ce dossier. Utilise la page de connexion "
                            + "ou « Mot de passe oublié ? ».");
            case TROP_DE_DEMANDES -> messageLabel.setText(
                    "Trop de demandes récentes. Réessaie dans quelques minutes.");
        }
    }

    private void ouvrirSaisieCode(String carte, String email, String personnel) {
        try {
            Stage stage = (Stage) numeroCarteField.getScene().getWindow();
            ActivationCodeController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/activation-code.fxml",
                    "Mentor-UADB - Validation du code");
            controller.setContexte(carte, email, personnel);
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir la saisie du code.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "Mentor-UADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à la connexion.");
        }
    }
}
    // ==================== FIN CLASSE ====================
