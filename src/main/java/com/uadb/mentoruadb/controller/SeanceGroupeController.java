package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.DialogueUtil;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/** Contrôleur lié à fxml/seance-groupe.fxml. */
public class SeanceGroupeController {

    @FXML private ComboBox<Matiere> matiereComboBox;
    @FXML private DatePicker dateSeancePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private ComboBox<String> modaliteComboBox;
    @FXML private TextField lieuField;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final MentoratService mentoratService = new MentoratService();

    private Etudiant etudiantConnecte;
    private Mentor mentorConnecte;
    private String prenomConnecte;

    @FXML
    public void initialize() {
        modaliteComboBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL"));
        MessageLabel.rendreDynamique(messageLabel);
    }

    /** Appelée manuellement depuis DashboardMentorController après le chargement de cet écran. */
    public void setContexte(Etudiant etudiant, Mentor mentor, String prenom) {
        this.etudiantConnecte = etudiant;
        this.mentorConnecte = mentor;
        this.prenomConnecte = prenom;

        try {
            List<Matiere> matieres = expertiseDao.findMatieresByMentor(mentor.getIdMentor());
            matiereComboBox.getItems().setAll(matieres);
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières.");
        }
    }

    @FXML
    private void onCreerClick() {
        Matiere matiere = matiereComboBox.getValue();
        String modalite = modaliteComboBox.getValue();
        String lieu = lieuField.getText();

        if (matiere == null || dateSeancePicker.getValue() == null || heureDebutField.getText().isBlank()
                || heureFinField.getText().isBlank() || modalite == null || lieu.isBlank()) {
            messageLabel.setText("Remplis tous les champs.");
            return;
        }
        if (dateSeancePicker.getValue().isBefore(LocalDate.now())) {
            messageLabel.setText("La date de la séance ne peut pas être dans le passé.");
            return;
        }

        try {
            DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime heureDebut = LocalTime.parse(heureDebutField.getText().trim(), format);
            LocalTime heureFin = LocalTime.parse(heureFinField.getText().trim(), format);

            if (!heureFin.isAfter(heureDebut)) {
                messageLabel.setText("L'heure de fin doit être après l'heure de début.");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Créer la séance de groupe");
            confirmation.setContentText(null);
            DialogueUtil.contenu(confirmation, "Créer une séance de groupe en " + matiere.getNom() + " le "
                    + dateSeancePicker.getValue() + " de " + heureDebut + " à " + heureFin + " (" + modalite + ", "
                    + lieu + ") ? Tous les étudiants ayant une demande acceptée sur cette matière seront invités.");
            if (!confirmation.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
                return;
            }

            mentoratService.creerSeanceDeGroupe(mentorConnecte.getIdMentor(), matiere.getIdMatiere(),
                    dateSeancePicker.getValue(), heureDebut, heureFin, modalite, lieu);
            messageLabel.setText("Séance de groupe créée et étudiants invités. Redirection...");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(event -> onRetourClick());
            pause.play();
        } catch (DateTimeParseException e) {
            messageLabel.setText("Format d'heure invalide — utilise HH:mm (ex: 14:30).");
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la création.");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            DashboardMentorController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-mentor.fxml", "Mentor-UADB - Espace mentor"
            );
            controller.chargerDonnees(etudiantConnecte, mentorConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord mentor.");
        }
    }
}
// ==================== FIN CLASSE ====================
