package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.ChampTelephone;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import com.uadb.mentoruadb.util.Validateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Optional;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contrôleur lié à fxml/profil.fxml.
 * Sépare les données modifiables (coordonnées, photo, profil mentor) des données
 * contrôlées par l'établissement (identité, email, rôle, statut, filière, niveau, carte).
 * Le rôle et le statut de validation ne sont jamais modifiables depuis cet écran.
 */
public class ProfilController {

    // Données contrôlées (lecture seule)
    @FXML private Label nomCompletLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statutLabel;
    @FXML private Label filiereLabel;
    @FXML private Label niveauLabel;
    @FXML private Label numeroCarteLabel;

    // Coordonnées et photo (modifiables)
    @FXML private TextField telephoneField;
    @FXML private TextField emailRecuperationField;
    @FXML private Label photoLabel;

    // Profil mentor (modifiable, si applicable)
    @FXML private VBox carteMentor;
    @FXML private TextArea biographieField;
    @FXML private TextField experienceField;
    @FXML private ComboBox<String> modePreferenceComboBox;
    @FXML private TextField nombreMaxMentoresField;

    @FXML private Label statutCandidatureLabel;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final MentorDao mentorDao = new MentorDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final NiveauDao niveauDao = new NiveauDao();

    private Utilisateur utilisateurConnecte;
    private Etudiant etudiantConnecte;
    private Mentor mentorConnecte;
    private String prenomConnecte;
    private java.io.File photoChoisie;

    @FXML
    public void initialize() {
        modePreferenceComboBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL", "LES_DEUX"));
        MessageLabel.rendreDynamique(messageLabel);
        ChampTelephone.formater(telephoneField);
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

            // Identité et compte (lecture seule)
            nomCompletLabel.setText(utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom());
            emailLabel.setText(utilisateurConnecte.getEmail());
            roleLabel.setText(utilisateurConnecte.getRole());
            statutLabel.setText(utilisateurConnecte.getStatut());

            // Informations académiques (lecture seule)
            filiereLabel.setText(filiereDao.findById(etudiant.getIdFiliere())
                    .map(f -> f.getNom()).orElse("Non renseignée"));
            niveauLabel.setText(niveauDao.findById(etudiant.getIdNiveau())
                    .map(n -> n.getLibelle()).orElse("Non renseigné"));
            String carte = etudiant.getNumeroCarte();
            numeroCarteLabel.setText(carte == null || carte.isBlank() ? "Non renseigné" : carte);

            // Coordonnées (modifiables)
            telephoneField.setText(Validateur.formaterTelephone(utilisateurConnecte.getTelephone()));
            emailRecuperationField.setText(utilisateurConnecte.getEmailRecuperation());
            photoLabel.setText(utilisateurConnecte.getPhoto() != null ? utilisateurConnecte.getPhoto() : "Aucune photo");

            Optional<Mentor> mentorOpt = mentorDao.findByEtudiant(etudiant.getIdEtudiant());
            // La section mentor n'apparaît que pour un mentor VALIDÉ.
            // Un candidat (en attente / refusé) n'y a pas accès : ses infos sont saisies dans « Devenir mentor ».
            boolean estMentor = mentorOpt.isPresent() && "VALIDE".equals(mentorOpt.get().getStatutValidation());
            boolean candidatureEnCours = mentorOpt.isPresent() && !estMentor;

            statutCandidatureLabel.setVisible(candidatureEnCours);
            statutCandidatureLabel.setManaged(candidatureEnCours);
            if (candidatureEnCours) {
                statutCandidatureLabel.setText("EN_ATTENTE".equals(mentorOpt.get().getStatutValidation())
                        ? "Candidature mentor en attente de validation par un administrateur."
                        : "Ta candidature mentor a été refusée.");
            }

            carteMentor.setVisible(estMentor);
            carteMentor.setManaged(estMentor);

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

            String telephone = Validateur.normaliserTelephone(telephoneField.getText());
            if (!telephone.isEmpty() && !Validateur.telephoneValide(telephone)) {
                messageLabel.setText("Numéro de téléphone invalide : 9 chiffres attendus (ex. 77 123 45 67).");
                return;
            }
            utilisateurConnecte.setTelephone(telephone.isEmpty() ? null : telephone);

            String emailRecuperation = emailRecuperationField.getText() == null
                    ? "" : emailRecuperationField.getText().trim();
            if (!emailRecuperation.isEmpty() && !emailRecuperation.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                messageLabel.setText("L'email de récupération n'est pas valide.");
                return;
            }
            utilisateurConnecte.setEmailRecuperation(emailRecuperation.isEmpty() ? null : emailRecuperation);

            utilisateurDao.update(utilisateurConnecte);

            // Le numéro de carte, la filière, le niveau, le rôle et le statut ne sont pas modifiables ici :
            // ils sont contrôlés par l'établissement.

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
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "Mentor-UADB - Tableau de bord"
            );
            controller.chargerDonnees(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord étudiant.");
        }
    }
}
// ==================== FIN CLASSE ====================
