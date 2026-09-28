package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.UfrDao;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.model.Ufr;
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

/** Contrôleur lié à fxml/inscription.fxml (choix en cascade : UFR → Filière → Niveau). */
public class InscriptionController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField numeroCarteField;
    @FXML private PasswordField motDePasseField;
    @FXML private TextField motDePasseVisibleField;
    @FXML private Button toggleMotDePasseButton;
    @FXML private ComboBox<Ufr> ufrComboBox;
    @FXML private ComboBox<Filiere> filiereComboBox;
    @FXML private ComboBox<Niveau> niveauComboBox;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final InscriptionService inscriptionService = new InscriptionService();
    private final UfrDao ufrDao = new UfrDao();
    private final FiliereDao filiereDao = new FiliereDao();

    private boolean motDePasseVisible = false;

    @FXML
    public void initialize() {
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());

        try {
            ufrComboBox.getItems().setAll(ufrDao.findAll());
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des UFR.");
        }

        filiereComboBox.setDisable(true);
        niveauComboBox.setDisable(true);

        // Étape 1 : l'UFR choisie détermine les filières proposées
        ufrComboBox.valueProperty().addListener((obs, ancienne, nouvelle) -> {
            filiereComboBox.getItems().clear();
            filiereComboBox.setValue(null);
            niveauComboBox.getItems().clear();
            niveauComboBox.setValue(null);
            niveauComboBox.setDisable(true);
            messageLabel.setText("");

            if (nouvelle == null) {
                filiereComboBox.setDisable(true);
                return;
            }

            try {
                List<Filiere> filieres = filiereDao.findByUfr(nouvelle.getIdUfr());
                filiereComboBox.getItems().setAll(filieres);
                filiereComboBox.setDisable(filieres.isEmpty());
                if (filieres.isEmpty()) {
                    messageLabel.setText("Aucune filière n'est encore enregistrée pour cette UFR.");
                }
            } catch (SQLException e) {
                messageLabel.setText("Erreur de chargement des filières.");
            }
        });

        // Étape 2 : la filière choisie détermine les niveaux proposés
        filiereComboBox.valueProperty().addListener((obs, ancienne, nouvelle) -> {
            niveauComboBox.getItems().clear();
            niveauComboBox.setValue(null);

            if (nouvelle == null) {
                niveauComboBox.setDisable(true);
                return;
            }

            try {
                List<Niveau> niveaux = filiereDao.findNiveauxByFiliere(nouvelle.getIdFiliere());
                niveauComboBox.getItems().setAll(niveaux);
                niveauComboBox.setDisable(niveaux.isEmpty());
                if (niveaux.isEmpty()) {
                    messageLabel.setText("Aucun niveau n'est disponible pour cette filière pour le moment.");
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
        String telephone = telephoneField.getText();
        String numeroCarte = numeroCarteField.getText();
        String motDePasse = motDePasseField.getText();
        Filiere filiere = filiereComboBox.getValue();
        Niveau niveau = niveauComboBox.getValue();

        if (nom.isBlank() || prenom.isBlank() || email.isBlank() || telephone.isBlank()
                || numeroCarte.isBlank() || motDePasse.isBlank() || filiere == null || niveau == null) {
            messageLabel.setText("Veuillez remplir tous les champs (UFR, filière et niveau compris).");
            return;
        }

        try {
            inscriptionService.inscrireEtudiant(
                    nom, prenom, email, motDePasse, filiere.getIdFiliere(), niveau.getIdNiveau(), telephone, numeroCarte
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