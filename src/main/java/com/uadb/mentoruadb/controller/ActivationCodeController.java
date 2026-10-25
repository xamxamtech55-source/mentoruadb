package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.service.ActivationService;
import com.uadb.mentoruadb.util.ChampErreur;
import com.uadb.mentoruadb.util.ChampTelephone;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import jakarta.mail.MessagingException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Étape 2 : validation du code. Aucun compte n'est créé ici ; le ticket
 * retourné ouvre l'étape de définition du mot de passe.
 */
public class ActivationCodeController {

    @FXML private TextField codeField;
    @FXML private Label emailLabel;
    @FXML private Label messageLabel;
    @FXML private Button verifierButton;
    @FXML private Button renvoyerButton;
    @FXML private Button retourButton;

    private final ActivationService activationService = new ActivationService();

    private String numeroCarte;
    private String email;
    private String emailPersonnel;
    private Timeline decompte;

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        ChampTelephone.chiffres(codeField, 6);
        codeField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(codeField));
        codeField.setOnAction(e -> onVerifierClick());
    }

    public void setContexte(String numeroCarte, String email, String emailPersonnel) {
        this.numeroCarte = numeroCarte;
        this.email = email;
        this.emailPersonnel = emailPersonnel;
        emailLabel.setText("Code envoyé à l'adresse email que tu as indiquée.");
        demarrerDecompte();
    }

    @FXML
    private void onVerifierClick() {
        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (!code.matches("\\d{6}")) {
            ChampErreur.marquer(codeField);
            messageLabel.setText("Le code doit contenir exactement 6 chiffres.");
            return;
        }

        verifierButton.setDisable(true);
        try {
            ActivationService.VerificationResultat resultat =
                    activationService.verifierCode(numeroCarte, email, code);
            if (resultat.ticketEmis()) {
                ouvrirChoixMotDePasse(resultat);
            } else {
                messageLabel.setText("Un compte existe déjà pour ce dossier. Connecte-toi.");
                verifierButton.setDisable(false);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            ChampErreur.marquer(codeField);
            messageLabel.setText(e.getMessage());
            verifierButton.setDisable(false);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la vérification du code.");
            verifierButton.setDisable(false);
        }
    }

    @FXML
    private void onRenvoyerClick() {
        renvoyerButton.setDisable(true);
        messageLabel.setText("Envoi d'un nouveau code en cours…");

        new Thread(() -> {
            try {
                ActivationService.DemandeResultat resultat =
                        activationService.demanderCode(numeroCarte, email, emailPersonnel);
                Platform.runLater(() -> traiterRenvoi(resultat));
            } catch (IllegalArgumentException | IllegalStateException | MessagingException e) {
                Platform.runLater(() -> {
                    messageLabel.setText(e.getMessage());
                    demarrerDecompte();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    messageLabel.setText("Erreur lors du renvoi du code.");
                    demarrerDecompte();
                });
            }
        }, "activation-renvoi").start();
    }

    private void traiterRenvoi(ActivationService.DemandeResultat resultat) {
        switch (resultat.statut()) {
            case ENVOYE -> {
                codeField.clear();
                messageLabel.setText("Un nouveau code a été envoyé. Il expire dans "
                        + ActivationService.DUREE_CODE_MINUTES + " minutes.");
                demarrerDecompte();
            }
            case TROP_DE_DEMANDES -> {
                messageLabel.setText("Trop de demandes récentes. Réessaie dans quelques minutes.");
                demarrerDecompte();
            }
            case COMPTE_EXISTANT -> messageLabel.setText(
                    "Un compte existe déjà pour ce dossier. Utilise la page de connexion.");
            case INFORMATIONS_INCORRECTES -> messageLabel.setText(
                    "Les informations du dossier ne correspondent plus. Recommence la première connexion.");
            case INSCRIPTION_INACTIVE -> messageLabel.setText(
                    "Ton inscription universitaire n'est pas active. Contacte le service de scolarité.");
        }
    }

    private void demarrerDecompte() {
        if (decompte != null) {
            decompte.stop();
        }
        renvoyerButton.setDisable(true);
        int[] restantes = {ActivationService.INTERVALLE_MINIMAL_SECONDES};
        renvoyerButton.setText("Renvoyer le code (" + restantes[0] + " s)");
        decompte = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            restantes[0]--;
            if (restantes[0] <= 0) {
                decompte.stop();
                renvoyerButton.setDisable(false);
                renvoyerButton.setText("Renvoyer le code");
            } else {
                renvoyerButton.setText("Renvoyer le code (" + restantes[0] + " s)");
            }
        }));
        decompte.setCycleCount(Timeline.INDEFINITE);
        decompte.play();
    }

    private void ouvrirChoixMotDePasse(ActivationService.VerificationResultat resultat) {
        try {
            Stage stage = (Stage) codeField.getScene().getWindow();
            ActivationMotDePasseController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/activation-mot-de-passe.fxml",
                    "Mentor-UADB - Définir mon mot de passe");
            controller.setContexte(resultat.ticket(), resultat.carte(), resultat.email(),
                    resultat.prenom());
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir la définition du mot de passe.");
            verifierButton.setDisable(false);
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
