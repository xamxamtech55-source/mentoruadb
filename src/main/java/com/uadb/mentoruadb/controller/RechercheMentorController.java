package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.dto.MentorSuggestionVue;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur lié à fxml/recherche-mentor.fxml :
 * suggestions automatiques + recherche par matière.
 */
public class RechercheMentorController {

    @FXML
    private TableView<MentorSuggestionVue> suggestionsTable;

    @FXML
    private TableColumn<MentorSuggestionVue, String> colSuggNom;

    @FXML
    private TableColumn<MentorSuggestionVue, String> colSuggNiveau;

    @FXML
    private TableColumn<MentorSuggestionVue, String> colSuggMatieres;

    @FXML
    private TableColumn<MentorSuggestionVue, String> colSuggNote;

    @FXML
    private ComboBox<Matiere> matiereComboBox;

    @FXML
    private TableView<MentorVue> mentorsTable;

    @FXML
    private TableColumn<MentorVue, String> colMentorNom;

    @FXML
    private TableColumn<MentorVue, String> colMentorFiliere;

    @FXML
    private TableColumn<MentorVue, String> colMentorNiveau;

    @FXML
    private ImageView photoMentorImageView;

    @FXML
    private TextArea profilTextArea;

    @FXML
    private Label messageLabel;

    @FXML
    private Button retourButton;

    private final FiliereDao filiereDao = new FiliereDao();
    private final MatiereDao matiereDao = new MatiereDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final MentorDao mentorDao = new MentorDao();
    private final MentoratService mentoratService = new MentoratService();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;

    // Mentor actuellement choisi,
    // qu'il vienne des suggestions ou de la recherche par matière
    private Integer idMentorChoisi = null;
    private String nomMentorChoisi = null;
    private String resumeMentorChoisi = null;

    @FXML
    public void initialize() {

        // =========================================================
        // CONFIGURATION TABLEAU DES SUGGESTIONS
        // =========================================================

        colSuggNom.setCellValueFactory(
                new PropertyValueFactory<>("nomMentor")
        );

        colSuggNiveau.setCellValueFactory(
                new PropertyValueFactory<>("libelleNiveau")
        );

        colSuggMatieres.setCellValueFactory(
                new PropertyValueFactory<>("matieres")
        );

        colSuggNote.setCellValueFactory(
                new PropertyValueFactory<>("noteMoyenne")
        );

        // =========================================================
        // CONFIGURATION TABLEAU DES MENTORS
        // =========================================================

        colMentorNom.setCellValueFactory(
                new PropertyValueFactory<>("nomMentor")
        );

        colMentorFiliere.setCellValueFactory(
                new PropertyValueFactory<>("nomFiliere")
        );

        colMentorNiveau.setCellValueFactory(
                new PropertyValueFactory<>("libelleNiveau")
        );

        // =========================================================
        // SÉLECTION DANS LES SUGGESTIONS
        // =========================================================

        suggestionsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, ancien, sugg) -> {

                    if (sugg != null) {

                        // Désélectionner le tableau de recherche
                        mentorsTable.getSelectionModel().clearSelection();

                        idMentorChoisi = sugg.getIdMentor();
                        nomMentorChoisi = sugg.getNomMentor();

                        resumeMentorChoisi =
                                sugg.getNomMentor()
                                        + " — "
                                        + sugg.getNomFiliere()
                                        + ", "
                                        + sugg.getLibelleNiveau();
                    }
                });

        // =========================================================
        // SÉLECTION DANS LA RECHERCHE PAR MATIÈRE
        // =========================================================

        mentorsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, ancien, mentor) -> {

                    if (mentor != null) {

                        // Désélectionner les suggestions
                        suggestionsTable.getSelectionModel().clearSelection();

                        idMentorChoisi = mentor.getIdMentor();
                        nomMentorChoisi = mentor.getNomMentor();

                        resumeMentorChoisi =
                                mentor.getNomMentor()
                                        + " — "
                                        + mentor.getNomFiliere()
                                        + ", "
                                        + mentor.getLibelleNiveau();
                    }
                });
    }

    /**
     * Appelée manuellement depuis DashboardEtudiantController
     * après le chargement de cet écran.
     */
    public void setEtudiantConnecte(
            Etudiant etudiant,
            String prenom
    ) {

        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;

        try {

            // =====================================================
            // CHARGEMENT DES MATIÈRES DE LA FILIÈRE
            // =====================================================

            List<Matiere> matieres =
                    filiereDao.findMatieresByFiliere(
                            etudiant.getIdFiliere()
                    );

            matiereComboBox.getItems().setAll(matieres);

            if (matieres.isEmpty()) {

                messageLabel.setText(
                        "Aucune matière n'est rattachée à ta filière pour l'instant."
                );
            }

            // =====================================================
            // CHARGEMENT DES SUGGESTIONS DE MENTORS
            // =====================================================

            List<MentorSuggestionVue> suggestions =
                    mentorDao.findSuggestionsPourFiliere(
                            etudiant.getIdFiliere(),
                            etudiant.getIdEtudiant()
                    );

            suggestionsTable.setItems(
                    FXCollections.observableArrayList(suggestions)
            );

            if (suggestions.isEmpty() && !matieres.isEmpty()) {

                messageLabel.setText(
                        "Aucun mentor validé ne couvre encore les matières de ta filière."
                );
            }

        } catch (SQLException e) {

            messageLabel.setText(
                    "Erreur de chargement des matières ou des suggestions."
            );

            e.printStackTrace();
        }
    }

    /**
     * Recherche des mentors validés par matière.
     */
    @FXML
    private void onRechercherClick() {

        Matiere matiere = matiereComboBox.getValue();

        if (matiere == null) {

            messageLabel.setText(
                    "Choisis une matière d'abord."
            );

            return;
        }

        try {

            List<MentorVue> mentors =
                    expertiseDao.findMentorsValidesParMatiere(
                            matiere.getIdMatiere()
                    );

            mentorsTable.setItems(
                    FXCollections.observableArrayList(mentors)
            );

            // Nettoyer l'ancien profil
            profilTextArea.clear();
            photoMentorImageView.setImage(null);

            // Réinitialiser le mentor sélectionné
            idMentorChoisi = null;
            nomMentorChoisi = null;
            resumeMentorChoisi = null;

            // Désélectionner les deux tableaux
            suggestionsTable.getSelectionModel().clearSelection();
            mentorsTable.getSelectionModel().clearSelection();

            if (mentors.isEmpty()) {

                messageLabel.setText(
                        "Aucun mentor validé pour cette matière pour le moment."
                );

            } else {

                messageLabel.setText("");
            }

        } catch (SQLException e) {

            messageLabel.setText(
                    "Erreur lors de la recherche."
            );

            e.printStackTrace();
        }
    }

    /**
     * Affiche le profil du mentor sélectionné.
     */
    @FXML
    private void onVoirProfilClick() {

        if (idMentorChoisi == null) {

            messageLabel.setText(
                    "Sélectionne un mentor (suggéré ou trouvé par recherche) d'abord."
            );

            return;
        }

        // =========================================================
        // CHARGEMENT DE LA PHOTO
        // =========================================================

        photoMentorImageView.setImage(null);

        try {

            Optional<String> cheminPhoto =
                    mentorDao.findPhotoByMentor(idMentorChoisi);

            if (cheminPhoto.isPresent()
                    && !cheminPhoto.get().isBlank()) {

                File fichierPhoto =
                        new File(cheminPhoto.get());

                if (fichierPhoto.exists()) {

                    photoMentorImageView.setImage(
                            new Image(
                                    fichierPhoto.toURI().toString()
                            )
                    );
                }
            }

        } catch (SQLException ignored) {

            // Pas de photo :
            // le cadre reste vide, ce n'est pas bloquant.
        }

        // =========================================================
        // CHARGEMENT DES INFORMATIONS DU PROFIL
        // =========================================================

        try {

            // -----------------------------------------------------
            // RÉCUPÉRATION DES EXPERTISES
            // -----------------------------------------------------

            List<Expertise> expertises =
                    expertiseDao.findByMentor(
                            idMentorChoisi
                    );

            StringBuilder matieresMaitrisees =
                    new StringBuilder();

            for (Expertise expertise : expertises) {

                Optional<Matiere> matiereOpt =
                        matiereDao.findById(
                                expertise.getIdMatiere()
                        );

                matiereOpt.ifPresent(m -> {

                    if (matieresMaitrisees.length() > 0) {

                        matieresMaitrisees.append(", ");
                    }

                    matieresMaitrisees.append(
                            m.getNom()
                    );
                });
            }

            // -----------------------------------------------------
            // RÉCUPÉRATION DES ÉVALUATIONS
            // -----------------------------------------------------

            List<EvaluationVue> evaluations =
                    evaluationDao.findByMentorAvecDetails(
                            idMentorChoisi
                    );

            String resumeNote;

            if (evaluations.isEmpty()) {

                resumeNote =
                        "Pas encore d'évaluation.";

            } else {

                double moyenne =
                        evaluations.stream()
                                .mapToInt(EvaluationVue::getNote)
                                .average()
                                .orElse(0);

                resumeNote =
                        String.format(
                                "%.1f/5 (%d avis)",
                                moyenne,
                                evaluations.size()
                        );
            }

            // -----------------------------------------------------
            // CONSTRUCTION DU PROFIL
            // -----------------------------------------------------

            StringBuilder texte =
                    new StringBuilder();

            texte.append(
                    resumeMentorChoisi
            ).append("\n\n");

            texte.append(
                    "Matières maîtrisées : "
            ).append(
                    matieresMaitrisees.length() > 0
                            ? matieresMaitrisees
                            : "aucune déclarée"
            ).append("\n\n");

            texte.append(
                    "Note moyenne : "
            ).append(
                    resumeNote
            );

            // -----------------------------------------------------
            // DERNIERS COMMENTAIRES
            // -----------------------------------------------------

            if (!evaluations.isEmpty()) {

                texte.append(
                        "\n\nDerniers commentaires :\n"
                );

                evaluations.stream()
                        .limit(3)
                        .forEach(e -> {

                            String commentaire =
                                    e.getCommentaire();

                            texte.append("- ")
                                    .append(e.getNote())
                                    .append("/5");

                            if (commentaire != null
                                    && !commentaire.isBlank()) {

                                texte.append(" : ")
                                        .append(commentaire);
                            }

                            texte.append("\n");
                        });
            }

            profilTextArea.setText(
                    texte.toString()
            );

        } catch (SQLException e) {

            messageLabel.setText(
                    "Erreur lors du chargement du profil."
            );

            e.printStackTrace();
        }
    }

    /**
     * Envoie une demande de mentorat.
     */
    @FXML
    private void onDemanderClick() {

        Matiere matiere =
                matiereComboBox.getValue();

        // =========================================================
        // VÉRIFICATION DU MENTOR
        // =========================================================

        if (idMentorChoisi == null) {

            messageLabel.setText(
                    "Sélectionne d'abord un mentor (suggéré ou trouvé par recherche)."
            );

            return;
        }

        // =========================================================
        // VÉRIFICATION DE LA MATIÈRE
        // =========================================================

        if (matiere == null) {

            messageLabel.setText(
                    "Choisis la matière pour laquelle tu demandes de l'aide."
            );

            return;
        }

        try {

            // =====================================================
            // VÉRIFIER QUE LE MENTOR MAÎTRISE LA MATIÈRE
            // =====================================================

            boolean maitriseLaMatiere =
                    expertiseDao.findByMentor(idMentorChoisi)
                            .stream()
                            .anyMatch(
                                    e -> e.getIdMatiere()
                                            == matiere.getIdMatiere()
                            );

            if (!maitriseLaMatiere) {

                messageLabel.setText(
                        nomMentorChoisi
                                + " ne maîtrise pas « "
                                + matiere.getNom()
                                + " ». Choisis une autre matière "
                                + "(voir son profil pour la liste)."
                );

                return;
            }

            // =====================================================
            // CRÉATION DE LA DEMANDE
            // =====================================================

            mentoratService.creerDemande(
                    etudiantConnecte.getIdEtudiant(),
                    idMentorChoisi,
                    matiere.getIdMatiere()
            );

            messageLabel.setText(
                    "Demande envoyée à "
                            + nomMentorChoisi
                            + " !"
            );

        } catch (IllegalArgumentException e) {

            messageLabel.setText(
                    e.getMessage()
            );

        } catch (SQLException e) {

            messageLabel.setText(
                    "Erreur lors de l'envoi de la demande."
            );

            e.printStackTrace();
        }
    }

    /**
     * Retour au tableau de bord étudiant.
     */
    @FXML
    private void onRetourClick() {

        try {

            Stage stage =
                    (Stage) retourButton
                            .getScene()
                            .getWindow();

            DashboardEtudiantController controller =
                    SceneNavigator.switchToAndGetController(
                            stage,
                            "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml",
                            "MentorUADB - Tableau de bord"
                    );

            controller.chargerDonnees(
                    etudiantConnecte,
                    prenomConnecte
            );

        } catch (IOException e) {

            messageLabel.setText(
                    "Impossible de revenir au tableau de bord."
            );

            e.printStackTrace();
        }
    }
}