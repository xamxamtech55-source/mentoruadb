package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dto.MentorDetailVue;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Contrôleur lié à fxml/liste-mentors.fxml : consultation de tous les mentors par l'admin. */
public class ListeMentorsController {

    private static final String TOUS = "Tous les statuts";

    @FXML private ChoiceBox<String> filtreStatut;
    @FXML private TableView<MentorDetailVue> mentorsTable;
    @FXML private TableColumn<MentorDetailVue, String> colMentor;
    @FXML private TableColumn<MentorDetailVue, String> colFiliere;
    @FXML private TableColumn<MentorDetailVue, String> colNiveau;
    @FXML private TableColumn<MentorDetailVue, String> colMatieres;
    @FXML private TableColumn<MentorDetailVue, String> colStatut;
    @FXML private Label identiteValue;
    @FXML private Label emailValue;
    @FXML private Label telephoneValue;
    @FXML private Label carteValue;
    @FXML private Label formationValue;
    @FXML private Label matieresValue;
    @FXML private Label experienceValue;
    @FXML private Label modeValue;
    @FXML private Label capaciteValue;
    @FXML private Label biographieValue;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final MentorDao mentorDao = new MentorDao();
    private List<MentorDetailVue> tousLesMentors = List.of();

    @FXML
    public void initialize() {
        colMentor.setCellValueFactory(new PropertyValueFactory<>("nomComplet"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));
        colStatut.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(libelleStatut(cellData.getValue().getStatutValidation())));
        colStatut.setCellFactory(col -> coloredStatutCell());

        filtreStatut.getItems().setAll(TOUS, "Validés", "En attente", "Refusés");
        filtreStatut.setValue(TOUS);
        filtreStatut.valueProperty().addListener((obs, ancien, nouveau) -> appliquerFiltre());

        mentorsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, mentor) ->
                afficherFiche(mentor));

        charger();
    }

    /** Colore la cellule de statut pour distinguer(validé / en attente / refusé) d'un coup d'œil. */
    private static TableCell<MentorDetailVue, String> coloredStatutCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vide) {
                super.updateItem(item, vide);
                if (vide || item == null) {
                    setText(null);
                    setStyleClass(null);
                } else {
                    setText(item);
                    setStyleClass(switch (item) {
                        case "Validé" -> "statut-valide";
                        case "En attente" -> "statut-attente";
                        default -> "statut-refuse";
                    });
                }
            }

            private void setStyleClass(String classe) {
                getStyleClass().removeIf(c -> c.startsWith("statut-"));
                if (classe != null) {
                    getStyleClass().add(classe);
                }
            }
        };
    }

    private void charger() {
        try {
            tousLesMentors = mentorDao.findTousAvecDetails();
            appliquerFiltre();
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Erreur de chargement de la liste des mentors.");
        }
    }

    private void appliquerFiltre() {
        String choix = filtreStatut.getValue();
        List<MentorDetailVue> filtres = tousLesMentors.stream()
                .filter(m -> correspondAuFiltre(m.getStatutValidation(), choix))
                .toList();

        mentorsTable.setItems(FXCollections.observableArrayList(filtres));
        afficherFiche(null);
        messageLabel.setText(filtres.size() + " mentor(s) affiché(s).");
    }

    private static boolean correspondAuFiltre(String statut, String choix) {
        if (choix == null || TOUS.equals(choix)) {
            return true;
        }
        return switch (choix) {
            case "Validés" -> "VALIDE".equals(statut);
            case "En attente" -> "EN_ATTENTE".equals(statut);
            case "Refusés" -> "REFUSE".equals(statut);
            default -> true;
        };
    }

    private void afficherFiche(MentorDetailVue mentor) {
        boolean selectionne = mentor != null;

        if (!selectionne) {
            identiteValue.setText("Sélectionne un mentor dans le tableau pour afficher sa fiche.");
            emailValue.setText("");
            telephoneValue.setText("");
            carteValue.setText("");
            formationValue.setText("");
            matieresValue.setText("");
            experienceValue.setText("");
            modeValue.setText("");
            capaciteValue.setText("");
            biographieValue.setText("");
            return;
        }

        identiteValue.setText(mentor.getNomComplet());
        emailValue.setText(ouNonRenseigne(mentor.getEmail()));
        telephoneValue.setText(ouNonRenseigne(mentor.getTelephone()));
        carteValue.setText(ouNonRenseigne(mentor.getNumeroCarte()));
        formationValue.setText(mentor.getNomFiliere() + " — " + mentor.getLibelleNiveau());
        matieresValue.setText(ouNonRenseigne(mentor.getMatieres()));
        experienceValue.setText(ouNonRenseigne(mentor.getExperience()));
        modeValue.setText(ouNonRenseigne(libelleMode(mentor.getModePreference())));
        capaciteValue.setText(mentor.getNombreMaxMentores() != null
                ? mentor.getNombreMaxMentores() + " étudiant(s) maximum"
                : "Non renseignée");
        biographieValue.setText(ouNonRenseigne(mentor.getBiographie()));
    }

    private static String ouNonRenseigne(String valeur) {
        return valeur == null || valeur.isBlank() ? "Non renseigné" : valeur;
    }

    private static String libelleStatut(String statut) {
        if (statut == null) {
            return "";
        }
        return switch (statut) {
            case "VALIDE" -> "Validé";
            case "EN_ATTENTE" -> "En attente";
            case "REFUSE" -> "Refusé";
            default -> statut;
        };
    }

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
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "Mentor-UADB - Administration");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord admin.");
        }
    }
}