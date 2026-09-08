package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

/** Contrôleur lié à fxml/login.fxml. */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField motDePasseField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onConnexionClick() {
        String email = emailField.getText();
        String motDePasse = motDePasseField.getText();

        if (email.isBlank() || motDePasse.isBlank()) {
            messageLabel.setText("Veuillez renseigner l'email et le mot de passe.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.connecter(email, motDePasse);
            if (utilisateur == null) {
                messageLabel.setText("Email ou mot de passe incorrect.");
            } else {
                messageLabel.setText("Bienvenue " + utilisateur.getPrenom()
                        + " (" + utilisateur.getRole() + ")");
                // TODO: rediriger vers le dashboard correspondant au rôle
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données.");
        }
    }
}
