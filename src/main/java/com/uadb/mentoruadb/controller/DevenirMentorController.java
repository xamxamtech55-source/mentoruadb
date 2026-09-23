package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Expertise;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrôleur lié à fxml/devenir-mentor.fxml. */
public class DevenirMentorController {

    @FXML private ListView<Matiere> matieresListView;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final MatiereDao matiereDao = new MatiereDao();
    private final MentorDao mentorDao = new MentorDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;
    @FXML
    public void initialize() {
        matieresListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        try {
            List<Matiere> matieres = matiereDao.findAll();
            matieresListView.setItems(FXCollections.observableArrayList(matieres));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières.");
        }
    }

    /** Appelée manuellement depuis DashboardEtudiantController après le chargement de cet écran. */
    public void setEtudiantConnecte(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;
    }
    @FXML
    private void onCandidaterClick() {
        List<Matiere> matieresChoisies = matieresListView.getSelectionModel().getSelectedItems();

        if (matieresChoisies.isEmpty()) {
            messageLabel.setText("Choisis au moins une matière.");
            return;
        }

        try {
            Optional<Mentor> mentorExistant = mentorDao.findByEtudiant(etudiantConnecte.getIdEtudiant());
            if (mentorExistant.isPresent()) {
                messageLabel.setText("Tu as déjà une candidature mentor (statut : "
                        + mentorExistant.get().getStatutValidation() + ").");
                return;
            }

            Mentor mentor = new Mentor(0, etudiantConnecte.getIdEtudiant(), "EN_ATTENTE");
            mentor = mentorDao.create(mentor);

            for (Matiere matiere : matieresChoisies) {
                expertiseDao.ajouter(new Expertise(mentor.getIdMentor(), matiere.getIdMatiere()));
            }

            messageLabel.setText("Candidature envoyée ! En attente de validation par un administrateur.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'envoi de la candidature.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "MentorUADB - Tableau de bord"
            );
            controller.chargerDonnees(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord.");
        }
    }
}