package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.UfrDao;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.dto.MentorRechercheVue;
import com.uadb.mentoruadb.dto.MentorSuggestionVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Expertise;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.model.Ufr;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
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
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contrôleur lié à fxml/recherche-mentor.fxml :
 * filtres UFR → filière → niveau → matière, suggestions pour la filière de l'étudiant,
 * et résultats enrichis (identité, filière, spécialités, note réelle, disponibilité).
 */
public class RechercheMentorController {

    // ---- Suggestions ----
    @FXML private TableView<MentorSuggestionVue> suggestionsTable;
    @FXML private TableColumn<MentorSuggestionVue, String> colSuggNom;
    @FXML private TableColumn<MentorSuggestionVue, String> colSuggNiveau;
    @FXML private TableColumn<MentorSuggestionVue, String> colSuggMatieres;
    @FXML private TableColumn<MentorSuggestionVue, String> colSuggNote;

    // ---- Filtres ----
    @FXML private ComboBox<Ufr> ufrComboBox;
    @FXML private ComboBox<Filiere> filiereComboBox;
    @FXML private ComboBox<Niveau> niveauComboBox;
    @FXML private ComboBox<Matiere> matiereComboBox;
    @FXML private Button rechercherButton;
    @FXML private ProgressIndicator rechercheProgress;

    // ---- Résultats ----
    @FXML private TableView<MentorRechercheVue> mentorsTable;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorNom;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorFiliere;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorNiveau;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorMatieres;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorNote;
    @FXML private TableColumn<MentorRechercheVue, String> colMentorDispo;
    @FXML private Label resultatsVideLabel;

    // ---- Profil et actions ----
    @FXML private ImageView photoMentorImageView;
    @FXML private TextArea profilTextArea;
    @FXML private Button envoyerDemandeButton;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UfrDao ufrDao = new UfrDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final MatiereDao matiereDao = new MatiereDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final MentorDao mentorDao = new MentorDao();
    private final MentoratService mentoratService = new MentoratService();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;

    // Mentor actuellement choisi (suggestion ou résultat de recherche)
    private Integer idMentorChoisi = null;
    private String nomMentorChoisi = null;
    private String resumeMentorChoisi = null;

    @FXML
    public void initialize() {
        // Tableau des suggestions
        colSuggNom.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colSuggNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colSuggMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));
        colSuggNote.setCellValueFactory(new PropertyValueFactory<>("noteMoyenne"));

        // Tableau des résultats
        colMentorNom.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colMentorFiliere.setCellValueFactory(new PropertyValueFactory<>("nomFiliere"));
        colMentorNiveau.setCellValueFactory(new PropertyValueFactory<>("libelleNiveau"));
        colMentorMatieres.setCellValueFactory(new PropertyValueFactory<>("matieres"));
        colMentorNote.setCellValueFactory(new PropertyValueFactory<>("noteTexte"));
        colMentorDispo.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));

        // Sélection dans les suggestions
        suggestionsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, sugg) -> {
            if (sugg != null) {
                mentorsTable.getSelectionModel().clearSelection();
                choisirMentor(sugg.getIdMentor(),
                        sugg.getNomMentor() + " — " + sugg.getNomFiliere() + ", " + sugg.getLibelleNiveau());
            }
        });

        // Sélection dans les résultats
        mentorsTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, mentor) -> {
            if (mentor != null) {
                suggestionsTable.getSelectionModel().clearSelection();
                choisirMentor(mentor.getIdMentor(),
                        mentor.getNomMentor() + " — " + mentor.getNomFiliere() + ", " + mentor.getLibelleNiveau());
            }
        });

        // Cascades de filtres
        ufrComboBox.valueProperty().addListener((obs, ancien, ufr) -> {
            chargerFilieres(ufr);
            filiereComboBox.setValue(null);
            chargerNiveaux(null);
            chargerMatieres(null);
        });
        filiereComboBox.valueProperty().addListener((obs, ancien, filiere) -> chargerNiveaux(filiere));

        MessageLabel.rendreDynamique(messageLabel);
        initialiserCascade();
    }

    private void initialiserCascade() {
        try {
            List<Ufr> ufrs = ufrDao.findAll();
            ufrComboBox.setItems(FXCollections.observableArrayList(ufrs));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des UFR.");
        }
    }

    private void choisirMentor(int idMentor, String resume) {
        idMentorChoisi = idMentor;
        nomMentorChoisi = resume.split(" — ")[0];
        resumeMentorChoisi = resume;
        envoyerDemandeButton.setDisable(false);
    }

    private void chargerFilieres(Ufr ufr) {
        if (ufr == null) {
            filiereComboBox.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            filiereComboBox.setItems(FXCollections.observableArrayList(
                    filiereDao.findByUfr(ufr.getIdUfr())));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des filières.");
        }
    }

    private void chargerNiveaux(Filiere filiere) {
        if (filiere == null) {
            niveauComboBox.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            niveauComboBox.setItems(FXCollections.observableArrayList(
                    filiereDao.findNiveauxByFiliere(filiere.getIdFiliere())));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des niveaux.");
        }
    }

    /** Les matières suivent la filière choisie ; sans filière, on montre toutes les matières. */
    private void chargerMatieres(Filiere filiere) {
        try {
            List<Matiere> matieres = filiere == null
                    ? matiereDao.findAll()
                    : filiereDao.findMatieresByFiliere(filiere.getIdFiliere());
            matiereComboBox.setItems(FXCollections.observableArrayList(matieres));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières.");
        }
    }

    /** Appelée manuellement depuis DashboardEtudiantController après le chargement de cet écran. */
    public void setEtudiantConnecte(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;

        try {
            // Pré-sélectionne l'UFR, la filière et le niveau de l'étudiant.
            Optional<Filiere> filiereEtudiant = filiereDao.findById(etudiant.getIdFiliere());
            if (filiereEtudiant.isPresent()) {
                Filiere f = filiereEtudiant.get();
                ufrComboBox.getItems().stream()
                        .filter(u -> u.getIdUfr() == f.getIdUfr())
                        .findFirst().ifPresent(ufrComboBox::setValue);
                filiereComboBox.getItems().stream()
                        .filter(ff -> ff.getIdFiliere() == f.getIdFiliere())
                        .findFirst().ifPresent(filiereComboBox::setValue);
                niveauComboBox.getItems().stream()
                        .filter(n -> n.getIdNiveau() == etudiant.getIdNiveau())
                        .findFirst().ifPresent(niveauComboBox::setValue);
            }
            chargerMatieres(filiereComboBox.getValue());

            // Suggestions pour la filière de l'étudiant.
            List<MentorSuggestionVue> suggestions = mentorDao.findSuggestionsPourFiliere(
                    etudiant.getIdFiliere(), etudiant.getIdEtudiant());
            suggestionsTable.setItems(FXCollections.observableArrayList(suggestions));

            if (suggestions.isEmpty()) {
                messageLabel.setText("Aucun mentor validé ne couvre encore les matières de ta filière.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières ou des suggestions.");
        }
    }

    /** Recherche les mentors validés correspondant aux filtres, sans bloquer l'interface. */
    @FXML
    private void onRechercherClick() {
        Ufr ufr = ufrComboBox.getValue();
        Filiere filiere = filiereComboBox.getValue();
        Niveau niveau = niveauComboBox.getValue();
        Matiere matiere = matiereComboBox.getValue();

        Integer idUfr = ufr != null ? ufr.getIdUfr() : null;
        Integer idFiliere = filiere != null ? filiere.getIdFiliere() : null;
        Integer idNiveau = niveau != null ? niveau.getIdNiveau() : null;
        Integer idMatiere = matiere != null ? matiere.getIdMatiere() : null;

        // État de chargement
        messageLabel.setText("");
        rechercheProgress.setVisible(true);
        rechercheProgress.setManaged(true);
        rechercherButton.setDisable(true);

        int idEtudiant = etudiantConnecte.getIdEtudiant();
        Task<List<MentorRechercheVue>> tache = new Task<>() {
            @Override
            protected List<MentorRechercheVue> call() throws SQLException {
                return mentorDao.rechercherMentorsValides(idUfr, idFiliere, idNiveau, idMatiere, idEtudiant);
            }
        };
        tache.setOnSucceeded(e -> {
            List<MentorRechercheVue> resultats = tache.getValue();
            afficherResultats(resultats);
            terminerRecherche();
        });
        tache.setOnFailed(e -> {
            messageLabel.setText("Erreur lors de la recherche. Réessaie.");
            terminerRecherche();
        });

        Thread thread = new Thread(tache, "recherche-mentor");
        thread.setDaemon(true);
        thread.start();
    }

    private void afficherResultats(List<MentorRechercheVue> resultats) {
        // Réinitialise la sélection et le profil affiché.
        idMentorChoisi = null;
        nomMentorChoisi = null;
        resumeMentorChoisi = null;
        profilTextArea.clear();
        photoMentorImageView.setImage(null);
        suggestionsTable.getSelectionModel().clearSelection();
        mentorsTable.getSelectionModel().clearSelection();
        envoyerDemandeButton.setDisable(false);

        boolean vide = resultats.isEmpty();
        resultatsVideLabel.setVisible(vide);
        resultatsVideLabel.setManaged(vide);
        mentorsTable.setVisible(!vide);
        mentorsTable.setManaged(!vide);

        if (vide) {
            resultatsVideLabel.setText("Aucun mentor validé ne correspond à ces critères.");
            messageLabel.setText("");
        } else {
            mentorsTable.setItems(FXCollections.observableArrayList(resultats));
            messageLabel.setText(resultats.size() + " mentor(s) trouvé(s).");
        }
    }

    private void terminerRecherche() {
        rechercheProgress.setVisible(false);
        rechercheProgress.setManaged(false);
        rechercherButton.setDisable(false);
    }

    /** Réinitialise les filtres et l'affichage. */
    @FXML
    private void onReinitialiserClick() {
        ufrComboBox.setValue(null);
        filiereComboBox.setValue(null);
        niveauComboBox.setValue(null);
        matiereComboBox.setValue(null);
        chargerMatieres(null);

        mentorsTable.setItems(FXCollections.observableArrayList());
        mentorsTable.setVisible(false);
        mentorsTable.setManaged(false);
        resultatsVideLabel.setVisible(true);
        resultatsVideLabel.setManaged(true);
        resultatsVideLabel.setText("Lance une recherche pour afficher des mentors.");

        idMentorChoisi = null;
        nomMentorChoisi = null;
        resumeMentorChoisi = null;
        profilTextArea.clear();
        photoMentorImageView.setImage(null);
        suggestionsTable.getSelectionModel().clearSelection();
        mentorsTable.getSelectionModel().clearSelection();
        envoyerDemandeButton.setDisable(false);

        messageLabel.setText("");
    }

    /** Affiche le profil détaillé du mentor sélectionné. */
    @FXML
    private void onVoirProfilClick() {
        if (idMentorChoisi == null) {
            messageLabel.setText("Sélectionne un mentor (suggéré ou dans les résultats) d'abord.");
            return;
        }

        photoMentorImageView.setImage(null);
        try {
            Optional<String> cheminPhoto = mentorDao.findPhotoByMentor(idMentorChoisi);
            if (cheminPhoto.isPresent() && !cheminPhoto.get().isBlank()) {
                File fichierPhoto = new File(cheminPhoto.get());
                if (fichierPhoto.exists()) {
                    photoMentorImageView.setImage(new Image(fichierPhoto.toURI().toString()));
                }
            }
        } catch (SQLException ignored) {
            // Pas de photo : le cadre reste vide, ce n'est pas bloquant.
        }

        try {
            List<Expertise> expertises = expertiseDao.findByMentor(idMentorChoisi);
            StringBuilder matieresMaitrisees = new StringBuilder();
            for (Expertise expertise : expertises) {
                Optional<Matiere> matiereOpt = matiereDao.findById(expertise.getIdMatiere());
                matiereOpt.ifPresent(m -> {
                    if (matieresMaitrisees.length() > 0) {
                        matieresMaitrisees.append(", ");
                    }
                    matieresMaitrisees.append(m.getNom());
                });
            }

            List<EvaluationVue> evaluations = evaluationDao.findByMentorAvecDetails(idMentorChoisi);
            String resumeNote;
            if (evaluations.isEmpty()) {
                resumeNote = "Pas encore d'évaluation.";
            } else {
                double moyenne = evaluations.stream().mapToInt(EvaluationVue::getNote).average().orElse(0);
                resumeNote = String.format("%.1f/5 (%d avis)", moyenne, evaluations.size());
            }

            StringBuilder texte = new StringBuilder();
            texte.append(resumeMentorChoisi).append("\n\n");
            texte.append("Matières maîtrisées : ")
                    .append(matieresMaitrisees.length() > 0 ? matieresMaitrisees : "aucune déclarée")
                    .append("\n\n");
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

    /** Envoie une demande de mentorat sur le mentor et la matière sélectionnés. */
    @FXML
    private void onDemanderClick() {
        Matiere matiere = matiereComboBox.getValue();

        if (idMentorChoisi == null) {
            messageLabel.setText("Sélectionne d'abord un mentor (suggéré ou dans les résultats).");
            return;
        }
        if (matiere == null) {
            messageLabel.setText("Choisis la matière pour laquelle tu demandes de l'aide.");
            return;
        }

        try {
            // Le mentor doit maîtriser la matière (contrôle aussi refait côté service).
            boolean maitriseLaMatiere = expertiseDao.findByMentor(idMentorChoisi).stream()
                    .anyMatch(e -> e.getIdMatiere() == matiere.getIdMatiere());
            if (!maitriseLaMatiere) {
                messageLabel.setText(nomMentorChoisi + " ne maîtrise pas « " + matiere.getNom()
                        + " ». Choisis une autre matière (voir son profil pour la liste).");
                return;
            }

            envoyerDemandeButton.setDisable(true);
            mentoratService.creerDemande(etudiantConnecte.getIdEtudiant(), idMentorChoisi, matiere.getIdMatiere());
            messageLabel.setText("Demande envoyée à " + nomMentorChoisi + " !");
            // Le bouton reste désactivé jusqu'à une nouvelle sélection : évite le double envoi.
        } catch (IllegalArgumentException | IllegalStateException e) {
            messageLabel.setText(e.getMessage());
            envoyerDemandeButton.setDisable(false);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'envoi de la demande.");
            envoyerDemandeButton.setDisable(false);
        }
    }

    /** Retour au tableau de bord étudiant. */
    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "Mentor-UADB - Tableau de bord");
            controller.chargerDonnees(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord étudiant.");
        }
    }
}
// ==================== FIN CLASSE ====================
