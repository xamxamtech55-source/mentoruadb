package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.service.InscriptionService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Contrôleur lié à fxml/inscription.fxml. */
public class InscriptionController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField motDePasseField;
    @FXML private TextField motDePasseVisibleField;
    @FXML private Button toggleMotDePasseButton;
    @FXML private ComboBox<Filiere> filiereComboBox;
    @FXML private ComboBox<Niveau> niveauComboBox;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final InscriptionService inscriptionService = new InscriptionService();
    private final FiliereDao filiereDao = new FiliereDao();
    private final NiveauDao niveauDao = new NiveauDao();

    private boolean motDePasseVisible = false;

    /** Appelée automatiquement par JavaFX juste après le chargement du FXML. */
    @FXML
    public void initialize() {
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());

        try {
            List<Filiere> filieres = filiereDao.findAll();
            filiereComboBox.getItems().setAll(filieres);
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur de chargement des filières.");
        }

        niveauComboBox.setDisable(true);

        filiereComboBox.valueProperty().addListener((obs, ancienneFiliere, nouvelleFiliere) -> {
            niveauComboBox.getItems().clear();
            niveauComboBox.setValue(null);

            if (nouvelleFiliere == null) {
                niveauComboBox.setDisable(true);
                return;
            }

            try {
                List<Niveau> niveaux = filiereDao.findNiveauxByFiliere(nouvelleFiliere.getIdFiliere());
                niveauComboBox.getItems().setAll(niveaux);
                niveauComboBox.setDisable(niveaux.isEmpty());
                if (niveaux.isEmpty()) {
                    messageLabel.setText("Aucun niveau disponible pour cette filière pour le moment.");
                }
            } catch (SQLException e) {
                messageLabel.setText("Erreur de chargement des niveaux.");
            }
        });
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
    private void onInscriptionClick() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String motDePasse = motDePasseField.getText();
        Filiere filiere = filiereComboBox.getValue();
        Niveau niveau = niveauComboBox.getValue();

        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || motDePasse.isBlank()
                || filiere == null || niveau == null) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            inscriptionService.inscrireEtudiant(
                    nom, prenom, email, motDePasse, filiere.getIdFiliere(), niveau.getIdNiveau()
            );
            messageLabel.setText("Compte créé avec succès ! Redirection vers la connexion...");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(event -> {
                try {
                    Stage stage = (Stage) retourButton.getScene().getWindow();
                    SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
                } catch (IOException e) {
                    messageLabel.setText("Compte créé — retourne manuellement à la connexion.");
                }
            });
            pause.play();
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la création du compte.");
        }
    }

    @FXML
    private void onRetourConnexionClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }
}