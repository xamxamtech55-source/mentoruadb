package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.FichierDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dao.SeanceParticipantDao;
import com.uadb.mentoruadb.dto.DemandeRecueVue;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.dto.SeanceGroupeVue;
import com.uadb.mentoruadb.dto.SeanceMentorVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Fichier;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Contrôleur lié à fxml/dashboard-mentor.fxml. */
public class DashboardMentorController {

    @FXML private Label bienvenueLabel;
    @FXML private Button deconnexionButton;
    @FXML private Button retourEtudiantButton;
    @FXML private Button seanceGroupeButton;

    @FXML private TableView<DemandeRecueVue> demandesTable;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeEtudiant;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeMatiere;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeDate;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeStatut;

    @FXML private DatePicker dateSeancePicker;
    @FXML private TextField heureDebutField;
    @FXML private TextField heureFinField;
    @FXML private ComboBox<String> modaliteComboBox;
    @FXML private TextField lieuField;

    @FXML private TableView<SeanceMentorVue> seancesTable;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceEtudiant;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceMatiere;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceDate;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceHeureDebut;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceHeureFin;
    @FXML private TableColumn<SeanceMentorVue, String> colSeanceStatut;

    @FXML private TableView<SeanceGroupeVue> seancesGroupeTable;
    @FXML private TableColumn<SeanceGroupeVue, String> colGroupeMatiere;
    @FXML private TableColumn<SeanceGroupeVue, String> colGroupeDate;
    @FXML private TableColumn<SeanceGroupeVue, String> colGroupeHeure;
    @FXML private TableColumn<SeanceGroupeVue, String> colGroupeStatut;
    @FXML private TableColumn<SeanceGroupeVue, String> colGroupeParticipation;

    @FXML private TableView<EvaluationVue> evaluationsTable;
    @FXML private TableColumn<EvaluationVue, String> colEvalEtudiant;
    @FXML private TableColumn<EvaluationVue, String> colEvalMatiere;
    @FXML private TableColumn<EvaluationVue, Integer> colEvalNote;
    @FXML private TableColumn<EvaluationVue, String> colEvalCommentaire;
    @FXML private TableColumn<EvaluationVue, String> colEvalDate;

    @FXML private ComboBox<Matiere> matiereFichierComboBox;
    @FXML private Label fichierSelectionneLabel;
    @FXML private TableView<Fichier> fichiersTable;
    @FXML private TableColumn<Fichier, String> colFichierNom;
    @FXML private TableColumn<Fichier, String> colFichierDate;

    @FXML private Label messageLabel;

    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final SeanceParticipantDao seanceParticipantDao = new SeanceParticipantDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final MentoratService mentoratService = new MentoratService();
    private final FichierDao fichierDao = new FichierDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();

    private Etudiant etudiantConnecte;
    private Mentor mentorConnecte;
    private String prenomConnecte;
    private java.io.File fichierChoisi;

    @FXML
    public void initialize() {
        colDemandeEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colDemandeMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colDemandeDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDemandeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        demandesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colSeanceEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colSeanceMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colSeanceDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colSeanceHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colSeanceHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colSeanceStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        seancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colGroupeMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colGroupeDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colGroupeHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colGroupeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colGroupeParticipation.setCellValueFactory(new PropertyValueFactory<>("participation"));
        seancesGroupeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colEvalEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colEvalMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colEvalNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colEvalCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colEvalDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        evaluationsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colFichierNom.setCellValueFactory(new PropertyValueFactory<>("nomFichier"));
        colFichierDate.setCellValueFactory(new PropertyValueFactory<>("dateUpload"));

        modaliteComboBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL"));
    }

    /** Appelée manuellement après le chargement de cet écran, pour lui transmettre le mentor connecté. */
    public void chargerDonnees(Etudiant etudiant, Mentor mentor, String prenom) {
        this.etudiantConnecte = etudiant;
        this.mentorConnecte = mentor;
        this.prenomConnecte = prenom;
        rafraichir();

        try {
            matiereFichierComboBox.getItems().setAll(expertiseDao.findMatieresByMentor(mentor.getIdMentor()));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des matières.");
        }
    }

    private void rafraichir() {
        bienvenueLabel.setText("Espace mentor — " + prenomConnecte);

        try {
            demandesTable.setItems(FXCollections.observableArrayList(
                    demandeDao.findByMentorAvecDetails(mentorConnecte.getIdMentor())
            ));
            seancesTable.setItems(FXCollections.observableArrayList(
                    seanceDao.findByMentorAvecDetails(mentorConnecte.getIdMentor())
            ));
            seancesGroupeTable.setItems(FXCollections.observableArrayList(
                    seanceDao.findGroupesByMentorAvecDetails(mentorConnecte.getIdMentor())
            ));
            evaluationsTable.setItems(FXCollections.observableArrayList(
                    evaluationDao.findByMentorAvecDetails(mentorConnecte.getIdMentor())
            ));
            fichiersTable.setItems(FXCollections.observableArrayList(
                    fichierDao.findByMentor(mentorConnecte.getIdMentor())
            ));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des données.");
        }
    }

    @FXML
    private void onAccepterClick() {
        DemandeRecueVue demande = demandesTable.getSelectionModel().getSelectedItem();
        if (demande == null) {
            messageLabel.setText("Sélectionne une demande dans le tableau d'abord.");
            return;
        }
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            messageLabel.setText("Cette demande a déjà été traitée.");
            return;
        }
        String modalite = modaliteComboBox.getValue();
        String lieu = lieuField.getText();
        if (dateSeancePicker.getValue() == null || heureDebutField.getText().isBlank()
                || heureFinField.getText().isBlank() || modalite == null || lieu.isBlank()) {
            messageLabel.setText("Renseigne date, heures (HH:mm), modalité et salle/lien.");
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

            mentoratService.accepterDemande(demande.getIdDemande(), dateSeancePicker.getValue(),
                    heureDebut, heureFin, modalite, lieu);
            messageLabel.setText("Demande acceptée, séance planifiée.");
            rafraichir();
        } catch (java.time.format.DateTimeParseException e) {
            messageLabel.setText("Format d'heure invalide — utilise HH:mm (ex: 14:30).");
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'acceptation.");
        }
    }

    @FXML
    private void onRefuserClick() {
        DemandeRecueVue demande = demandesTable.getSelectionModel().getSelectedItem();
        if (demande == null) {
            messageLabel.setText("Sélectionne une demande dans le tableau d'abord.");
            return;
        }
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            messageLabel.setText("Cette demande a déjà été traitée.");
            return;
        }

        try {
            mentoratService.refuserDemande(demande.getIdDemande());
            messageLabel.setText("Demande refusée.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du refus.");
        }
    }

    @FXML
    private void onMarquerRealiseeClick() {
        SeanceMentorVue seance = seancesTable.getSelectionModel().getSelectedItem();
        if (seance == null) {
            messageLabel.setText("Sélectionne une séance dans le tableau d'abord.");
            return;
        }

        try {
            mentoratService.marquerSeanceRealisee(seance.getIdSeance());
            messageLabel.setText("Séance marquée comme réalisée.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void onAnnulerClick() {
        SeanceMentorVue seance = seancesTable.getSelectionModel().getSelectedItem();
        if (seance == null) {
            messageLabel.setText("Sélectionne une séance dans le tableau d'abord.");
            return;
        }

        try {
            mentoratService.annulerSeance(seance.getIdSeance());
            messageLabel.setText("Séance annulée.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'annulation.");
        }
    }

    @FXML
    private void onVoirConfirmesClick() {
        SeanceGroupeVue seance = seancesGroupeTable.getSelectionModel().getSelectedItem();
        if (seance == null) {
            messageLabel.setText("Sélectionne une séance de groupe dans le tableau d'abord.");
            return;
        }

        try {
            List<String> noms = seanceParticipantDao.findNomsConfirmes(seance.getIdSeance());
            String titre = seance.getNomMatiere() + " (" + seance.getDateSeance() + ") — étudiants confirmés : ";
            messageLabel.setText(noms.isEmpty() ? titre + "aucun pour l'instant." : titre + String.join(", ", noms));
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des confirmations.");
        }
    }

    @FXML
    private void onEffacerDemandeClick() {
        List<DemandeRecueVue> selection = new ArrayList<>(demandesTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            messageLabel.setText("Sélectionne au moins une demande à effacer.");
            return;
        }
        try {
            for (DemandeRecueVue demande : selection) {
                demandeDao.masquerPourMentor(demande.getIdDemande());
            }
            messageLabel.setText(selection.size() + " demande(s) effacée(s) de ta vue.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onEffacerSeanceIndividuelleClick() {
        List<SeanceMentorVue> selection = new ArrayList<>(seancesTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            messageLabel.setText("Sélectionne au moins une séance à effacer.");
            return;
        }
        try {
            for (SeanceMentorVue seance : selection) {
                seanceDao.masquerIndividuellePourMentor(seance.getIdSeance());
            }
            messageLabel.setText(selection.size() + " séance(s) effacée(s) de ta vue.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onEffacerSeanceGroupeClick() {
        List<SeanceGroupeVue> selection = new ArrayList<>(seancesGroupeTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            messageLabel.setText("Sélectionne au moins une séance de groupe à effacer.");
            return;
        }
        try {
            for (SeanceGroupeVue seance : selection) {
                seanceDao.masquerGroupePourMentor(seance.getIdSeance());
            }
            messageLabel.setText(selection.size() + " séance(s) de groupe effacée(s) de ta vue.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onEffacerEvaluationClick() {
        List<EvaluationVue> selection = new ArrayList<>(evaluationsTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            messageLabel.setText("Sélectionne au moins une évaluation à effacer.");
            return;
        }
        try {
            for (EvaluationVue evaluation : selection) {
                evaluationDao.masquerPourMentor(evaluation.getIdEvaluation());
            }
            messageLabel.setText(selection.size() + " évaluation(s) effacée(s) de ta vue.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onChoisirFichierClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier à partager");
        fichierChoisi = fileChooser.showOpenDialog(deconnexionButton.getScene().getWindow());
        fichierSelectionneLabel.setText(fichierChoisi != null ? fichierChoisi.getName() : "Aucun fichier choisi");
    }

    @FXML
    private void onUploaderClick() {
        Matiere matiere = matiereFichierComboBox.getValue();
        if (fichierChoisi == null) {
            messageLabel.setText("Choisis d'abord un fichier.");
            return;
        }
        if (matiere == null) {
            messageLabel.setText("Choisis la matière associée à ce fichier.");
            return;
        }

        try {
            Path dossierDestination = Path.of("uploads", "mentor-" + mentorConnecte.getIdMentor());
            Files.createDirectories(dossierDestination);

            String nomUnique = System.currentTimeMillis() + "_" + fichierChoisi.getName();
            Path destination = dossierDestination.resolve(nomUnique);
            Files.copy(fichierChoisi.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            Fichier fichier = new Fichier(0, mentorConnecte.getIdMentor(), matiere.getIdMatiere(), null,
                    fichierChoisi.getName(), destination.toString(), LocalDate.now());
            fichierDao.create(fichier);

            messageLabel.setText("Fichier « " + fichierChoisi.getName() + " » partagé avec succès.");
            fichierChoisi = null;
            fichierSelectionneLabel.setText("Aucun fichier choisi");
            rafraichir();
        } catch (java.io.IOException e) {
            messageLabel.setText("Erreur lors de la copie du fichier.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'enregistrement en base.");
        }
    }

    @FXML
    private void onDeconnexionClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }

    @FXML
    private void onRetourEtudiantClick() {
        try {
            Stage stage = (Stage) retourEtudiantButton.getScene().getWindow();
            DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "MentorUADB - Tableau de bord"
            );
            controller.chargerDonnees(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord étudiant.");
        }
    }

    @FXML
    private void onSeanceGroupeClick() {
        try {
            Stage stage = (Stage) seanceGroupeButton.getScene().getWindow();
            SeanceGroupeController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/seance-groupe.fxml", "MentorUADB - Séance de groupe"
            );
            controller.setContexte(etudiantConnecte, mentorConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de séance de groupe.");
        }
    }
}