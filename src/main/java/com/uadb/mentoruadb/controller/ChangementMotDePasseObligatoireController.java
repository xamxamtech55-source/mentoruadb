package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.ChangementMotDePasseService;
import com.uadb.mentoruadb.util.ChampErreur;
import com.uadb.mentoruadb.util.MessageLabel;
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
 * Écran intermédiaire : un compte encore marqué must_change_password ne peut
 * ouvrir aucun espace avant d'avoir choisi un mot de passe personnel.
 */
public class ChangementMotDePasseObligatoireController {

    @FXML private PasswordField ancienField;
    @FXML private PasswordField nouveauField;
    @FXML private PasswordField confirmationField;
    @FXML private Label messageLabel;
    @FXML private Button validerButton;

    private final ChangementMotDePasseService service = new ChangementMotDePasseService();

    private Utilisateur utilisateur;

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        ancienField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(ancienField));
        nouveauField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(nouveauField));
        confirmationField.textProperty().addListener((o, a, n) -> ChampErreur.effacer(confirmationField));
    }

    public void initialiser(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        if (utilisateur != null) {
            messageLabel.setText("Choisis un mot de passe personnel pour " + utilisateur.getEmail() + ".");
        }
    }

    @FXML
    private void onValiderClick() {
        if (utilisateur == null) {
            messageLabel.setText("Session expirée. Reconnecte-toi.");
            return;
        }
        try {
            service.changerMotDePasse(utilisateur, ancienField.getText(),
                    nouveauField.getText(), confirmationField.getText());
            ouvrirEspace();
        } catch (IllegalArgumentException e) {
            ChampErreur.marquer(ancienField);
            ChampErreur.marquer(nouveauField);
            ChampErreur.marquer(confirmationField);
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'enregistrement du mot de passe.");
        }
    }

    private void ouvrirEspace() {
        try {
            Stage stage = (Stage) validerButton.getScene().getWindow();
            SessionNavigation.ouvrirEspace(stage, utilisateur);
        } catch (IOException | SQLException e) {
            messageLabel.setText("Mot de passe modifié, mais impossible d'ouvrir le tableau de bord. "
                    + "Reconnecte-toi.");
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        }
    }
}
// ==================== FIN CLASSE ====================
