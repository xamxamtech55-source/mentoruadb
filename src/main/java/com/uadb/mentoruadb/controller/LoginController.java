package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.AuthService;
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

/** Contrôleur lié à fxml/login.fxml. */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField motDePasseField;
    @FXML private TextField motDePasseVisibleField;
    @FXML private Button toggleMotDePasseButton;
    @FXML private Label messageLabel;
    @FXML private Button creerCompteButton;
    @FXML private Button motDePasseOublieButton;

    private final AuthService authService = new AuthService();
    private final EtudiantDao etudiantDao = new EtudiantDao();

    private boolean motDePasseVisible = false;

    @FXML
    public void initialize() {
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());
    }
    @FXML
    private void onToggleMotDePasseClick() {
        motDePasseVisible = !motDePasseVisible;
        motDePasseField.setVisible(!motDePasseVisible);
        motDePasseField.setManaged(!motDePasseVisible);
        motDePasseVisibleField.setVisible(motDePasseVisible);
        motDePasseVisibleField.setManaged(motDePasseVisible);
        toggleMotDePasseButton.setText(motDePasseVisible ? "🙈" : "👁");
    }

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
                return;
            }

            if ("ETUDIANT".equals(utilisateur.getRole()) || "MENTOR".equals(utilisateur.getRole())){
                Optional<Etudiant> etudiantOpt = etudiantDao.findByUtilisateur(utilisateur.getIdUtilisateur());
                if (etudiantOpt.isEmpty()) {
                    messageLabel.setText("Profil étudiant introuvable pour ce compte.");
                    return;
                }

                Stage stage = (Stage) emailField.getScene().getWindow();
                DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                        stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "MentorUADB - Tableau de bord"
                );
                controller.chargerDonnees(etudiantOpt.get(), utilisateur.getPrenom());
            } else if ("ADMIN".equals(utilisateur.getRole())) {
                Stage stage = (Stage) emailField.getScene().getWindow();
                DashboardAdminController controller = SceneNavigator.switchToAndGetController(
                        stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "MentorUADB - Administration"
                );
                controller.chargerDonnees(utilisateur.getPrenom());
            } else {
                messageLabel.setText("Bienvenue " + utilisateur.getPrenom()
                        + " (" + utilisateur.getRole() + ")");
            }
        } catch (SQLException e) {
        e.printStackTrace();
        messageLabel.setText("Erreur de connexion à la base de données.");
        
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir le tableau de bord.");
        }
    }

    @FXML
    private void onCreerCompteClick() {
        try {
            Stage stage = (Stage) creerCompteButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/inscription.fxml", "MentorUADB - Inscription");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran d'inscription.");
        }
    }

    @FXML
    private void onMotDePasseOublieClick() {
        try {
            Stage stage = (Stage) motDePasseOublieButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/mot-de-passe-oublie.fxml", "MentorUADB - Mot de passe oublié");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}