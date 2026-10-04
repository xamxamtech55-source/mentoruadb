package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dto.MentorDetailVue;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/** Contrôleur lié à fxml/validation-mentors.fxml. */
public class ValidationMentorsController {

    @FXML private TableView<MentorDetailVue> candidatsTable;
    @FXML private TableColumn<MentorDetailVue, String> colEtudiant;
    @FXML private TableColumn<MentorDetailVue, String> colFiliere;
    @FXML private TableColumn<MentorDetailVue, String> colNiveau;
    @FXML private TableColumn<MentorDetailVue, String> colMatieres;
    @FXML private Label identiteValue;
    @FXML private Label emailValue;
    @FXML private Label telephoneValue;
    @FXML private Label carteValue;
    @FXML private Label formationValue;
    @FXML private Label experienceDetailValue;
    @FXML private Label modeValue;
    @FXML private Label capaciteValue;
    @FXML private Label biographieArea;
    @FXML private Label messageLabel;
    @FXML private Button validerButton;
    @FXML private Button refuserButton;
    @FXML private Button retourButton;

    private final MentorDao mentorDao = new MentorDao();

    @FXML
    public void initialize() {
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));

        // Le dossier complet du candidat s'affiche dès qu'une ligne est sélectionnée.
        candidatsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, candidat) ->
                afficherDossier(candidat));

        rafraichir();
    }

    private void rafraichir() {
        try {
            candidatsTable.setItems(FXCollections.observableArrayList(mentorDao.findEnAttenteAvecDetails()));
            afficherDossier(null);
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur de chargement des candidatures.");
        }
    }

    /**
     * Remplit la fiche du candidat sélectionné. Sans sélection, les champs sont vidés
     * et les boutons Valider / Refuser sont désactivés pour éviter de valider la mauvaise ligne.
     */
    private void afficherDossier(MentorDetailVue candidat) {
        boolean selectionne = candidat != null;
        validerButton.setDisable(!selectionne);
        refuserButton.setDisable(!selectionne);

        if (!selectionne) {
            identiteValue.setText("Sélectionne une candidature dans le tableau pour afficher son dossier.");
            emailValue.setText("");
            telephoneValue.setText("");
            carteValue.setText("");
            formationValue.setText("");
            experienceDetailValue.setText("");
            modeValue.setText("");
            capaciteValue.setText("");
            biographieArea.setText("");
            return;
        }

        identiteValue.setText(candidat.getNomComplet());
        emailValue.setText(ouNonRenseigne(candidat.getEmail()));
        telephoneValue.setText(ouNonRenseigne(candidat.getTelephone()));
        carteValue.setText(ouNonRenseigne(candidat.getNumeroCarte()));
        formationValue.setText(candidat.getNomFiliere() + " — " + candidat.getLibelleNiveau());
        experienceDetailValue.setText(ouNonRenseigne(candidat.getExperience()));
        modeValue.setText(ouNonRenseigne(libelleMode(candidat.getModePreference())));
        capaciteValue.setText(candidat.getNombreMaxMentores() != null
                ? candidat.getNombreMaxMentores() + " étudiant(s) maximum"
                : "Non renseignée");
        biographieArea.setText(ouNonRenseigne(candidat.getBiographie()));
    }

    /** Affiche explicitement qu'une information manque, plutôt qu'un champ vide indistinct. */
    private static String ouNonRenseigne(String valeur) {
        return valeur == null || valeur.isBlank() ? "Non renseigné" : valeur;
    }

    /** Traduit le code de mode stocké en base pour un affichage lisible. */
    private static String libelleMode(String modePreference) {
        if (modePreference == null) {
            return "";
        }
        return switch (modePreference) {
            case "EN_LIGNE" -> "En ligne";
            case "PRESENTIEL" -> "Présentiel";
            case "LES_DEUX" -> "Les deux";
            default -> modePreference;
        };
    }

    @FXML
    private void onValiderClick() {
        changerStatut("VALIDE");
    }

    @FXML
    private void onRefuserClick() {
        changerStatut("REFUSE");
    }

    private void changerStatut(String statut) {
        MentorDetailVue candidat = candidatsTable.getSelectionModel().getSelectedItem();
        if (candidat == null) {
            messageLabel.setText("Sélectionne une candidature dans le tableau d'abord.");
            return;
        }

        try {
            Optional<Mentor> mentorOpt = mentorDao.findById(candidat.getIdMentor());
            if (mentorOpt.isEmpty()) {
                messageLabel.setText("Candidature introuvable.");
                return;
            }

            Mentor mentor = mentorOpt.get();
            mentor.setStatutValidation(statut);
            mentorDao.update(mentor);

            succes("Candidature de " + candidat.getNomComplet() + " ("
                    + candidat.getEmail() + ") : "
                    + ("VALIDE".equals(statut) ? "validée." : "refusée."));
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    /** Le message de confirmation n'est pas une erreur : il passe en vert. */
    private void succes(String texte) {
        messageLabel.getStyleClass().setAll("succes-label");
        messageLabel.setText(texte);
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "Mentor-UADB - Administration");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord admin.");
        }
    }
}