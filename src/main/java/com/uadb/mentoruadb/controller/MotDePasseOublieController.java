package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.PasswordUtil;
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

/**
 * Contrôleur lié à fxml/mot-de-passe-oublie.fxml.
 * La réinitialisation exige l'email ET le numéro de carte d'étudiant, pour vérifier que la
 * personne qui fait la demande est bien la propriétaire du compte (les deux sont demandés à
 * l'inscription, voir InscriptionController). Le message de résultat est volontairement le même
 * en cas d'email inconnu, de carte incorrecte ou de succès, pour ne rien révéler sur les
 * comptes existants (email enumeration).
 */
public class MotDePasseOublieController {

    @FXML private TextField emailField;
    @FXML private TextField numeroCarteField;
    @FXML private PasswordField nouveauMotDePasseField;
    @FXML private PasswordField confirmationField;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();

    private static final String MESSAGE_RESULTAT =
            "Si les informations correspondent à un compte, le mot de passe a été mis à jour.";

    @FXML
    private void onReinitialiserClick() {
        String email = emailField.getText();
        String numeroCarte = numeroCarteField.getText();
        String nouveauMotDePasse = nouveauMotDePasseField.getText();
        String confirmation = confirmationField.getText();

        if (email.isBlank() || numeroCarte.isBlank() || nouveauMotDePasse.isBlank() || confirmation.isBlank()) {
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
            if (identiteConfirmee(email, numeroCarte)) {
                Utilisateur utilisateur = utilisateurDao.findByEmail(email).orElseThrow();
                utilisateur.setMotDePasse(PasswordUtil.hacher(nouveauMotDePasse));
                utilisateurDao.update(utilisateur);
            }
            // Même message, que ça ait marché ou non : voir le commentaire de la classe.
            messageLabel.setText(MESSAGE_RESULTAT);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la réinitialisation.");
        }
    }

    /** Vrai si l'email correspond à un compte ET que le numéro de carte correspond à son profil étudiant. */
    private boolean identiteConfirmee(String email, String numeroCarte) throws SQLException {
        Optional<Utilisateur> resultatUtilisateur = utilisateurDao.findByEmail(email);
        if (resultatUtilisateur.isEmpty()) {
            return false;
        }
        Optional<Etudiant> resultatEtudiant = etudiantDao.findByUtilisateur(resultatUtilisateur.get().getIdUtilisateur());
        if (resultatEtudiant.isEmpty()) {
            return false;
        }
        String carteEnregistree = resultatEtudiant.get().getNumeroCarte();
        return carteEnregistree != null && carteEnregistree.equals(numeroCarte.trim());
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