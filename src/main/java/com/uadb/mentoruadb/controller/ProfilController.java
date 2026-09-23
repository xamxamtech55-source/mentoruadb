package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Optional;

/** Contrôleur lié à fxml/profil.fxml. Gère le profil utilisateur + étudiant + (si applicable) mentor. */
public class ProfilController {

    @FXML private Label emailLabel;
    @FXML private TextField telephoneField;
    @FXML private Label photoLabel;
    @FXML private TextField numeroCarteField;

    @FXML private Separator separateurMentor;
    @FXML private Label titreMentorLabel;
    @FXML private TextArea biographieField;
    @FXML private TextField experienceField;
    @FXML private ComboBox<String> modePreferenceComboBox;
    @FXML private TextField nombreMaxMentoresField;

    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final EtudiantDao etudiantDao = new EtudiantDao();
    private final MentorDao mentorDao = new MentorDao();

    private Utilisateur utilisateurConnecte;
    private Etudiant etudiantConnecte;
    private Mentor mentorConnecte;
    private String prenomConnecte;
    private java.io.File photoChoisie;

    @FXML
    public void initialize() {
        modePreferenceComboBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL", "LES_DEUX"));
    }

    /** Appelée manuellement depuis DashboardEtudiantController après le chargement de cet écran. */
    public void setContexte(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;

        try {
            Optional<Utilisateur> utilisateurOpt = utilisateurDao.findById(etudiant.getIdUtilisateur());
            if (utilisateurOpt.isEmpty()) {
                messageLabel.setText("Utilisateur introuvable.");
                return;
            }
            utilisateurConnecte = utilisateurOpt.get();

            emailLabel.setText("Email : " + utilisateurConnecte.getEmail());
            telephoneField.setText(utilisateurConnecte.getTelephone());
            photoLabel.setText(utilisateurConnecte.getPhoto() != null ? utilisateurConnecte.getPhoto() : "Aucune photo");
            numeroCarteField.setText(etudiant.getNumeroCarte());

            Optional<Mentor> mentorOpt = mentorDao.findByEtudiant(etudiant.getIdEtudiant());
            boolean estMentor = mentorOpt.isPresent();

            separateurMentor.setVisible(estMentor);
            separateurMentor.setManaged(estMentor);
            titreMentorLabel.setVisible(estMentor);
            titreMentorLabel.setManaged(estMentor);
            biographieField.setVisible(estMentor);
            biographieField.setManaged(estMentor);
            experienceField.setVisible(estMentor);
            experienceField.setManaged(estMentor);
            modePreferenceComboBox.setVisible(estMentor);
            modePreferenceComboBox.setManaged(estMentor);
            nombreMaxMentoresField.setVisible(estMentor);
            nombreMaxMentoresField.setManaged(estMentor);

            if (estMentor) {
                mentorConnecte = mentorOpt.get();
                biographieField.setText(mentorConnecte.getBiographie());
                experienceField.setText(mentorConnecte.getExperience());
                modePreferenceComboBox.setValue(mentorConnecte.getModePreference());
                nombreMaxMentoresField.setText(
                        mentorConnecte.getNombreMaxMentores() != null ? String.valueOf(mentorConnecte.getNombreMaxMentores()) : ""
                );
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement du profil.");
        }
    }

    @FXML
    private void onChoisirPhotoClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        photoChoisie = fileChooser.showOpenDialog(retourButton.getScene().getWindow());
        if (photoChoisie != null) {
            photoLabel.setText(photoChoisie.getName());
        }
    }

    @FXML
    private void onEnregistrerClick() {
        try {
            if (photoChoisie != null) {
                Path dossierDestination = Path.of("uploads", "profil-" + utilisateurConnecte.getIdUtilisateur());
                Files.createDirectories(dossierDestination);
                String nomUnique = System.currentTimeMillis() + "_" + photoChoisie.getName();
                Path destination = dossierDestination.resolve(nomUnique);
                Files.copy(photoChoisie.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                utilisateurConnecte.setPhoto(destination.toString());
                photoChoisie = null;
            }

            utilisateurConnecte.setTelephone(telephoneField.getText());
            utilisateurDao.update(utilisateurConnecte);

            etudiantConnecte.setNumeroCarte(numeroCarteField.getText());
            etudiantDao.update(etudiantConnecte);

            if (mentorConnecte != null) {
                mentorConnecte.setBiographie(biographieField.getText());
                mentorConnecte.setExperience(experienceField.getText());
                mentorConnecte.setModePreference(modePreferenceComboBox.getValue());

                String texteMax = nombreMaxMentoresField.getText();
                if (texteMax != null && !texteMax.isBlank()) {
                    try {
                        mentorConnecte.setNombreMaxMentores(Integer.parseInt(texteMax.trim()));
                    } catch (NumberFormatException ex) {
                        messageLabel.setText("Le nombre max de mentorés doit être un nombre entier.");
                        return;
                    }
                }
                mentorDao.update(mentorConnecte);
            }

            messageLabel.setText("Profil mis à jour avec succès.");
        } catch (java.io.IOException e) {
            messageLabel.setText("Erreur lors de la copie de la photo.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'enregistrement.");
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