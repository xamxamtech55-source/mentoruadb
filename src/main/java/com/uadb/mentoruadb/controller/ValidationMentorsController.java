package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dto.MentorDetailVue;
import com.uadb.mentoruadb.service.AdminService;
import com.uadb.mentoruadb.util.DialogueUtil;
import com.uadb.mentoruadb.util.MessageLabel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

/**
 * Contrôleur lié à fxml/validation-mentors.fxml (section du dashboard admin).
 * Les décisions passent par AdminService (garde de statut, RG20, notifications RG27).
 */
public class ValidationMentorsController {

    private static final String NIVEAU_INELIGIBLE = "Licence 1";

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
    @FXML private Label eligibiliteLabel;
    @FXML private Label aucuneCandidatureLabel;
    @FXML private Label messageLabel;
    @FXML private Button validerButton;
    @FXML private Button refuserButton;

    private final MentorDao mentorDao = new MentorDao();
    private final AdminService adminService = new AdminService();

    @FXML
    public void initialize() {
        colEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));

        // Le dossier complet du candidat s'affiche dès qu'une ligne est sélectionnée.
        candidatsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, candidat) ->
                afficherDossier(candidat));

        MessageLabel.rendreDynamique(messageLabel);
        rafraichir();
    }

    private void rafraichir() {
        try {
            var candidats = mentorDao.findEnAttenteAvecDetails();
            candidatsTable.setItems(FXCollections.observableArrayList(candidats));
            aucuneCandidatureLabel.setVisible(candidats.isEmpty());
            aucuneCandidatureLabel.setManaged(candidats.isEmpty());
            afficherDossier(null);
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des candidatures.");
        }
    }

    /**
     * Remplit la fiche du candidat sélectionné. Sans sélection, les champs sont vidés
     * et les boutons Valider / Refuser sont désactivés pour éviter de traiter la mauvaise ligne.
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
            eligibiliteLabel.setVisible(false);
            eligibiliteLabel.setManaged(false);
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

        // RG20 visible avant toute validation : Licence 1 ne peut pas être mentor.
        boolean ineligne = candidat.getLibelleNiveau() != null
                && NIVEAU_INELIGIBLE.equalsIgnoreCase(candidat.getLibelleNiveau());
        eligibiliteLabel.setVisible(ineligne);
        eligibiliteLabel.setManaged(ineligne);
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
        MentorDetailVue candidat = candidatsTable.getSelectionModel().getSelectedItem();
        if (candidat == null) {
            messageLabel.setText("Sélectionne une candidature dans le tableau d'abord.");
            return;
        }

        boolean confirme = confirmer("Valider la candidature",
                "Valider la candidature de " + candidat.getNomComplet() + " (" + candidat.getEmail() + ") ?\n"
                        + "Il deviendra mentor et pourra recevoir des demandes de mentorat.");
        if (!confirme) {
            return;
        }

        traiter(candidat, true);
    }

    @FXML
    private void onRefuserClick() {
        MentorDetailVue candidat = candidatsTable.getSelectionModel().getSelectedItem();
        if (candidat == null) {
            messageLabel.setText("Sélectionne une candidature dans le tableau d'abord.");
            return;
        }

        boolean confirme = confirmer("Refuser la candidature",
                "Refuser la candidature de " + candidat.getNomComplet() + " (" + candidat.getEmail() + ") ?\n"
                        + "Son dossier sera marqué refusé et il sera notifié.");
        if (!confirme) {
            return;
        }

        traiter(candidat, false);
    }

    private void traiter(MentorDetailVue candidat, boolean validation) {
        try {
            if (validation) {
                adminService.validerCandidature(candidat.getIdMentor());
            } else {
                adminService.refuserCandidature(candidat.getIdMentor());
            }
            succes("Candidature de " + candidat.getNomComplet() + " ("
                    + candidat.getEmail() + ") : "
                    + (validation ? "validée." : "refusée."));
            rafraichir();
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Candidature déjà traitée ou éligibilité refusée : on affiche le vrai motif.
            messageLabel.getStyleClass().setAll("message-label");
            messageLabel.setText(e.getMessage());
            rafraichir();
        } catch (SQLException e) {
            messageLabel.getStyleClass().setAll("message-label");
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    private static boolean confirmer(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(null);
        DialogueUtil.contenu(alert, contenu);
        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                == javafx.scene.control.ButtonType.OK;
    }

    /** Le message de confirmation n'est pas une erreur : il passe en vert. */
    private void succes(String texte) {
        messageLabel.getStyleClass().setAll("succes-label");
        messageLabel.setText(texte);
    }
}
