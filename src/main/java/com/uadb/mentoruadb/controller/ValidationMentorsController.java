package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dto.CandidatMentorVue;
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

    @FXML private TableView<CandidatMentorVue> candidatsTable;
    @FXML private TableColumn<CandidatMentorVue, String> colEtudiant;
    @FXML private TableColumn<CandidatMentorVue, String> colFiliere;
    @FXML private TableColumn<CandidatMentorVue, String> colNiveau;
    @FXML private TableColumn<CandidatMentorVue, String> colMatieres;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final MentorDao mentorDao = new MentorDao();

    @FXML
    public void initialize() {
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));
        rafraichir();
    }

    private void rafraichir() {
        try {
            candidatsTable.setItems(FXCollections.observableArrayList(mentorDao.findEnAttenteAvecDetails()));
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur de chargement des candidatures.");
        }
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
        CandidatMentorVue candidat = candidatsTable.getSelectionModel().getSelectedItem();
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

            messageLabel.setText("Candidature de " + candidat.getNomEtudiant() + " : " + statut + ".");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "MentorUADB - Administration");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord admin.");
        }
    }
}