package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/** Contrôleur lié à fxml/mot-de-passe-oublie.fxml. */
public class MotDePasseOublieController {

    @FXML private TextField emailField;
    @FXML private PasswordField nouveauMotDePasseField;
    @FXML private PasswordField confirmationField;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    @FXML
    private void onReinitialiserClick() {
        String email = emailField.getText();
        String nouveauMotDePasse = nouveauMotDePasseField.getText();
        String confirmation = confirmationField.getText();

        if (email.isBlank() || nouveauMotDePasse.isBlank() || confirmation.isBlank()) {
            messageLabel.setText("Remplis tous les champs.");
            return;
        }
        if (!nouveauMotDePasse.equals(confirmation)) {
            messageLabel.setText("Les deux mots de passe ne correspondent pas.");
            return;
        }
        if (!email.toLowerCase().endsWith("@uadb.edu.sn")) {
            messageLabel.setText("Utilise ton email institutionnel (@uadb.edu.sn).");
            return;
        }

        try {
            Optional<Utilisateur> resultat = utilisateurDao.findByEmail(email);
            if (resultat.isEmpty()) {
                messageLabel.setText("Aucun compte ne correspond à cet email.");
                return;
            }

            Utilisateur utilisateur = resultat.get();
            utilisateur.setMotDePasse(nouveauMotDePasse);
            utilisateurDao.update(utilisateur);

            messageLabel.setText("Mot de passe mis à jour ! Tu peux te connecter.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la réinitialisation.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à la connexion.");
        }
    }
}