package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.service.CandidatureMentorService;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/** Contrôleur lié à fxml/devenir-mentor.fxml. */
public class DevenirMentorController {

    @FXML private ListView<Matiere> matieresListView;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final FiliereDao filiereDao = new FiliereDao();
    private final CandidatureMentorService candidatureService = new CandidatureMentorService();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;

    @FXML
    public void initialize() {
        matieresListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        MessageLabel.rendreDynamique(messageLabel);
    }

    /** Appelée manuellement depuis DashboardEtudiantController après le chargement de cet écran. */
    public void setEtudiantConnecte(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;

        try {
            List<Matiere> matieres = filiereDao.findMatieresByFiliere(etudiant.getIdFiliere());
            matieresListView.setItems(FXCollections.observableArrayList(matieres));
            if (matieres.isEmpty()) {
                messageLabel.setText("Aucune matière n'est encore rattachée à ta filière — contacte un administrateur.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières.");
        }
    }

    @FXML
    private void onCandidaterClick() {
        List<Matiere> matieresChoisies = matieresListView.getSelectionModel().getSelectedItems();

        if (matieresChoisies.isEmpty()) {
            messageLabel.setText("Choisis au moins une matière.");
            return;
        }

        List<Integer> idMatieres = matieresChoisies.stream().map(Matiere::getIdMatiere).toList();

        try {
            candidatureService.soumettreCandidature(etudiantConnecte.getIdEtudiant(), idMatieres);
            messageLabel.setText("Candidature envoyée ! En attente de validation par un administrateur.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'envoi de la candidature.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "Mentor-UADB - Tableau de bord"
            );
            controller.chargerDonnees(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord étudiant.");
        }
    }
}
// ==================== FIN CLASSE ====================
