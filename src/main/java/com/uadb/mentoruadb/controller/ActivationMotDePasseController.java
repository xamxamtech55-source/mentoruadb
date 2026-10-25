package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.ActivationService;
import com.uadb.mentoruadb.service.AuthService;
import com.uadb.mentoruadb.util.ChampErreur;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Étape 3 : définition du mot de passe personnel. Le compte est créé de
 * manière transactionnelle à l'enregistrement, puis l'étudiant est authentifié
 * et envoyé directement à son tableau de bord.
 */
public class ActivationMotDePasseController {

    @FXML private PasswordField motDePasseField;
    @FXML private PasswordField confirmationField;
    @FXML private Label emailLabel;
    @FXML private Label messageLabel;
    @FXML private Button validerButton;

    private final ActivationService activationService = new ActivationService();
    private final AuthService authService = new AuthService();

    private String ticket;
    private String numeroCarte;
    private String email;

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        motDePasseField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(motDePasseField));
        confirmationField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(confirmationField));
    }

    public void setContexte(String ticket, String numeroCarte, String email, String prenom) {
        this.ticket = ticket;
        this.numeroCarte = numeroCarte;
        this.email = email;
        String qui = prenom == null || prenom.isBlank() ? "" : prenom + ", ";
        emailLabel.setText(qui + "définis le mot de passe de ton compte " + email);
    }

    @FXML
    private void onValiderClick() {
        String motDePasse = motDePasseField.getText();
        try {
            Utilisateur utilisateur = activationService.definirMotDePasse(
                    ticket, motDePasse, confirmationField.getText());

            // Connexion automatique après la création du compte.
            Utilisateur connecte = authService.connecter(utilisateur.getEmail(), numeroCarte, motDePasse);
            Stage stage = (Stage) validerButton.getScene().getWindow();
            if (connecte == null) {
                ouvrirConnexion(stage, utilisateur.getEmail());
                return;
            }
            SessionNavigation.ouvrirEspace(stage, connecte);
        } catch (IllegalArgumentException e) {
            ChampErreur.marquer(motDePasseField);
            ChampErreur.marquer(confirmationField);
            messageLabel.setText(e.getMessage());
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'enregistrement du mot de passe.");
        } catch (IOException e) {
            messageLabel.setText("Compte créé, mais impossible d'ouvrir le tableau de bord. "
                    + "Connecte-toi avec ton nouveau mot de passe.");
        }
    }

    private void ouvrirConnexion(Stage stage, String email) throws IOException {
        LoginController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/login.fxml", "Mentor-UADB - Connexion");
        controller.prefillerEmailEtMessage(email,
                "Compte activé. Connecte-toi avec ton nouveau mot de passe.");
    }
}
// ==================== FIN CLASSE ====================
