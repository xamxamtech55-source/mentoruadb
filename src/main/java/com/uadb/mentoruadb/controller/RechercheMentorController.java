package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.dto.MentorVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Expertise;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Contrôleur lié à fxml/recherche-mentor.fxml. */
public class RechercheMentorController {

    @FXML private ComboBox<Matiere> matiereComboBox;
    @FXML private TableView<MentorVue> mentorsTable;
    @FXML private TableColumn<MentorVue, String> colMentorNom;
    @FXML private TableColumn<MentorVue, String> colMentorFiliere;
    @FXML private TableColumn<MentorVue, String> colMentorNiveau;
    @FXML private TextArea profilTextArea;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final MatiereDao matiereDao = new MatiereDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final MentoratService mentoratService = new MentoratService();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;

    @FXML
    public void initialize() {
        colMentorNom.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colMentorFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colMentorNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));

        try {
            List<Matiere> matieres = matiereDao.findAll();
            matiereComboBox.getItems().setAll(matieres);
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
    private void onRechercherClick() {
        Matiere matiere = matiereComboBox.getValue();
        if (matiere == null) {
            messageLabel.setText("Choisis une matière d'abord.");
            return;
        }

        try {
            List<MentorVue> mentors = expertiseDao.findMentorsValidesParMatiere(matiere.getIdMatiere());
            mentorsTable.setItems(FXCollections.observableArrayList(mentors));
            profilTextArea.clear();

            if (mentors.isEmpty()) {
                messageLabel.setText("Aucun mentor validé pour cette matière pour le moment.");
            } else {
                messageLabel.setText("");
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la recherche.");
        }
    }

    @FXML
    private void onVoirProfilClick() {
        MentorVue mentor = mentorsTable.getSelectionModel().getSelectedItem();
        if (mentor == null) {
            messageLabel.setText("Sélectionne un mentor dans le tableau d'abord.");
            return;
        }

        try {
            List<Expertise> expertises = expertiseDao.findByMentor(mentor.getIdMentor());
            StringBuilder matieresMaitrisees = new StringBuilder();
            for (Expertise expertise : expertises) {
                Optional<Matiere> matiereOpt = matiereDao.findById(expertise.getIdMatiere());
                matiereOpt.ifPresent(m -> {
                    if (matieresMaitrisees.length() > 0) matieresMaitrisees.append(", ");
                    matieresMaitrisees.append(m.getNom());
                });
            }

            List<EvaluationVue> evaluations = evaluationDao.findByMentorAvecDetails(mentor.getIdMentor());
            String resumeNote;
            if (evaluations.isEmpty()) {
                resumeNote = "Pas encore d'évaluation.";
            } else {
                double moyenne = evaluations.stream().mapToInt(EvaluationVue::getNote).average().orElse(0);
                resumeNote = String.format("%.1f/5 (%d avis)", moyenne, evaluations.size());
            }

            StringBuilder texte = new StringBuilder();
            texte.append(mentor.getNomMentor()).append(" — ").append(mentor.getNomFiliere())
                    .append(", ").append(mentor.getLibelleNiveau()).append("\n\n");
            texte.append("Matières maîtrisées : ")
                    .append(matieresMaitrisees.length() > 0 ? matieresMaitrisees : "aucune déclarée").append("\n\n");
            texte.append("Note moyenne : ").append(resumeNote);

            if (!evaluations.isEmpty()) {
                texte.append("\n\nDerniers commentaires :\n");
                evaluations.stream().limit(3).forEach(e -> {
                    String commentaire = e.getCommentaire();
                    texte.append("- ").append(e.getNote()).append("/5");
                    if (commentaire != null && !commentaire.isBlank()) {
                        texte.append(" : ").append(commentaire);
                    }
                    texte.append("\n");
                });
            }

            profilTextArea.setText(texte.toString());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement du profil.");
        }
    }

    @FXML
    private void onDemanderClick() {
        MentorVue mentorSelectionne = mentorsTable.getSelectionModel().getSelectedItem();
        Matiere matiere = matiereComboBox.getValue();

        if (mentorSelectionne == null || matiere == null) {
            messageLabel.setText("Sélectionne un mentor dans le tableau d'abord.");
            return;
        }

        try {
            mentoratService.creerDemande(
                    etudiantConnecte.getIdEtudiant(), mentorSelectionne.getIdMentor(), matiere.getIdMatiere()
            );
            messageLabel.setText("Demande envoyée à " + mentorSelectionne.getNomMentor() + " !");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'envoi de la demande.");
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