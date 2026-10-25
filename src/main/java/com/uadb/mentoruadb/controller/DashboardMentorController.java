package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.DemandeSeanceDao;
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.ExpertiseDao;
import com.uadb.mentoruadb.dao.FichierDao;
import com.uadb.mentoruadb.dao.NotificationDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dao.SeanceParticipantDao;
import com.uadb.mentoruadb.dto.DemandeRecueVue;
import com.uadb.mentoruadb.dto.DemandeSeanceRecueVue;
import com.uadb.mentoruadb.dto.DestinataireVue;
import com.uadb.mentoruadb.dto.EvaluationVue;
import com.uadb.mentoruadb.dto.SeanceGroupeVue;
import com.uadb.mentoruadb.dto.SeanceMentorVue;
import com.uadb.mentoruadb.dto.SeanceResumeVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Fichier;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Notification;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
// ==================== FIN IMPORTS ====================
// ==================== DÉBUT CLASSE ====================
/** Contrôleur lié à fxml/dashboard-mentor.fxml. */
public class DashboardMentorController {

    @FXML private Label bienvenueLabel;
    @FXML private Label mentorHeaderLabel;
    @FXML private Button deconnexionButton;
    @FXML private Button hamburgerButton;
    @FXML private VBox sidebarPane;
    @FXML private Button profilButton;
    @FXML private Button retourEtudiantButton;
    @FXML private Button seanceGroupeButton;
    @FXML private Button notificationsButton;

    @FXML private TableView<DemandeRecueVue> demandesTable;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeEtudiant;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeMatiere;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeDate;
    @FXML private TableColumn<DemandeRecueVue, String> colDemandeStatut;

    @FXML private TableView<DemandeSeanceRecueVue> demandeSeanceTable;
    @FXML private TableColumn<DemandeSeanceRecueVue, String> colDsEtudiant;
    @FXML private TableColumn<DemandeSeanceRecueVue, String> colDsMatiere;
    @FXML private TableColumn<DemandeSeanceRecueVue, String> colDsDateSouhaitee;
    @FXML private TableColumn<DemandeSeanceRecueVue, String> colDsMessage;
    @FXML private TableColumn<DemandeSeanceRecueVue, String> colDsStatut;

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

    @FXML private TableView<SeanceResumeVue> prochainesSeancesTable;
    @FXML private TableColumn<SeanceResumeVue, String> colProchQui;
    @FXML private TableColumn<SeanceResumeVue, String> colProchMatiere;
    @FXML private TableColumn<SeanceResumeVue, String> colProchDate;
    @FXML private TableColumn<SeanceResumeVue, String> colProchDebut;
    @FXML private TableColumn<SeanceResumeVue, String> colProchFin;
    @FXML private TableColumn<SeanceResumeVue, String> colProchStatut;

    @FXML private TableView<SeanceResumeVue> termineesSeancesTable;
    @FXML private TableColumn<SeanceResumeVue, String> colTermQui;
    @FXML private TableColumn<SeanceResumeVue, String> colTermMatiere;
    @FXML private TableColumn<SeanceResumeVue, String> colTermDate;
    @FXML private TableColumn<SeanceResumeVue, String> colTermHeure;

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
    @FXML private ComboBox<DestinataireVue> destinataireFichierComboBox;
    @FXML private Label fichierSelectionneLabel;
    @FXML private TableView<Fichier> fichiersTable;
    @FXML private TableColumn<Fichier, String> colFichierNom;
    @FXML private TableColumn<Fichier, String> colFichierDate;

    @FXML private Label messageLabel;
    @FXML private StackPane sectionsPane;
    @FXML private ScrollPane scrollContenu;

    // Boutons d'action : activés uniquement sur une ligne sélectionnée au bon statut.
    @FXML private Button accepterDemandeButton;
    @FXML private Button refuserDemandeButton;
    @FXML private Button accepterSeanceButton;
    @FXML private Button refuserSeanceButton;
    @FXML private Button marquerRealiseeButton;
    @FXML private Button annulerSeanceButton;
    @FXML private Button marquerGroupeRealiseeButton;
    @FXML private Button annulerGroupeButton;

    // Zone de synthèse.
    @FXML private Label statDemandesValue;
    @FXML private Label statDemandesDetail;
    @FXML private Label statProchainesValue;
    @FXML private Label statProchainesDetail;
    @FXML private Label statTermineesValue;
    @FXML private Label statTermineesDetail;
    @FXML private Label statEvaluationsValue;
    @FXML private Label statEvaluationsDetail;
    @FXML private Label evaluationsResumeLabel;
    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final DemandeSeanceDao demandeSeanceDao = new DemandeSeanceDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final SeanceParticipantDao seanceParticipantDao = new SeanceParticipantDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final MentoratService mentoratService = new MentoratService();
    private final FichierDao fichierDao = new FichierDao();
    private final ExpertiseDao expertiseDao = new ExpertiseDao();
    private final NotificationDao notificationDao = new NotificationDao();

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

        colDsEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colDsMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colDsDateSouhaitee.setCellValueFactory(new PropertyValueFactory<>("dateSouhaitee"));
        colDsMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colDsStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        demandeSeanceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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

        colProchQui.setCellValueFactory(new PropertyValueFactory<>("qui"));
        colProchMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colProchDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colProchDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colProchFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colProchStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        prochainesSeancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colTermQui.setCellValueFactory(new PropertyValueFactory<>("qui"));
        colTermMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colTermDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colTermHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        termineesSeancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colEvalEtudiant.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));
        colEvalMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colEvalNote.setCellValueFactory(new PropertyValueFactory<>("note"));
        colEvalCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));
        colEvalDate.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        evaluationsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colFichierNom.setCellValueFactory(new PropertyValueFactory<>("nomFichier"));
        colFichierDate.setCellValueFactory(new PropertyValueFactory<>("dateUpload"));

        modaliteComboBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL"));

        // Recharge la liste "Partager avec" à chaque changement de matière (elle dépend des
        // étudiants qui ont une demande acceptée pour CETTE matière avec ce mentor).
        matiereFichierComboBox.valueProperty().addListener((obs, ancienne, nouvelle) -> rafraichirDestinataires(nouvelle));

        MessageLabel.rendreDynamique(messageLabel);
        hamburgerButton.setTooltip(new Tooltip("Masquer le menu"));

        // Les boutons d'action ne sont actifs que sur une ligne sélectionnée (et au bon statut).
        demandesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancienne, nouvelle) -> majEtatBoutons());
        demandeSeanceTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancienne, nouvelle) -> majEtatBoutons());
        seancesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancienne, nouvelle) -> majEtatBoutons());
        seancesGroupeTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, ancienne, nouvelle) -> majEtatBoutons());
        majEtatBoutons();
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
        mentorHeaderLabel.setText(prenomConnecte + " — mentor validé");

        try {
            List<DemandeRecueVue> demandes = demandeDao.findByMentorAvecDetails(mentorConnecte.getIdMentor());
            List<DemandeSeanceRecueVue> demandesSeance = demandeSeanceDao.findByMentorAvecDetails(mentorConnecte.getIdMentor());
            List<SeanceMentorVue> seances = seanceDao.findByMentorAvecDetails(mentorConnecte.getIdMentor());
            List<SeanceGroupeVue> seancesGroupe = seanceDao.findGroupesByMentorAvecDetails(mentorConnecte.getIdMentor());
            List<EvaluationVue> evaluations = evaluationDao.findByMentorAvecDetails(mentorConnecte.getIdMentor());

            demandesTable.setItems(FXCollections.observableArrayList(demandes));
            demandeSeanceTable.setItems(FXCollections.observableArrayList(demandesSeance));
            seancesTable.setItems(FXCollections.observableArrayList(seances));
            seancesGroupeTable.setItems(FXCollections.observableArrayList(seancesGroupe));
            evaluationsTable.setItems(FXCollections.observableArrayList(evaluations));
            fichiersTable.setItems(FXCollections.observableArrayList(
                    fichierDao.findByMentor(mentorConnecte.getIdMentor())
            ));

            // Tableaux de synthèse : prochaines séances (individuelles + groupe) et séances terminées.
            List<SeanceResumeVue> toutesLesSeances = new ArrayList<>();
            for (SeanceMentorVue seance : seances) {
                toutesLesSeances.add(resumeDe(seance));
            }
            for (SeanceGroupeVue seance : seancesGroupe) {
                toutesLesSeances.add(resumeDe(seance));
            }

            List<SeanceResumeVue> prochaines = toutesLesSeances.stream()
                    .filter(s -> "PLANIFIEE".equals(s.getStatut()))
                    .filter(s -> s.getDateSeance() != null && !s.getDateSeance().isBefore(LocalDate.now()))
                    .sorted(Comparator.comparing(SeanceResumeVue::getDateSeance)
                            .thenComparing(s -> s.getHeureDebut() == null ? LocalTime.MIN : s.getHeureDebut()))
                    .toList();
            List<SeanceResumeVue> terminees = toutesLesSeances.stream()
                    .filter(s -> "REALISEE".equals(s.getStatut()))
                    .filter(s -> s.getDateSeance() != null)
                    .sorted(Comparator.comparing(SeanceResumeVue::getDateSeance).reversed())
                    .toList();

            prochainesSeancesTable.setItems(FXCollections.observableArrayList(prochaines));
            termineesSeancesTable.setItems(FXCollections.observableArrayList(terminees));

            majStatistiques(demandes, demandesSeance, prochaines, terminees, evaluations);

            int nbNonLues = notificationDao.findNonLuesByUtilisateur(etudiantConnecte.getIdUtilisateur()).size();
            if (nbNonLues > 0) {
                notificationsButton.setText("🔔 " + nbNonLues + " notification(s) — clique pour voir");
                notificationsButton.setVisible(true);
                notificationsButton.setManaged(true);
            } else {
                notificationsButton.setVisible(false);
                notificationsButton.setManaged(false);
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des données.");
        }

        majEtatBoutons();
    }

    /** Convertit une séance individuelle en ligne de synthèse. */
    private SeanceResumeVue resumeDe(SeanceMentorVue seance) {
        return new SeanceResumeVue(seance.getIdSeance(), seance.getNomEtudiant(), seance.getNomMatiere(),
                seance.getDateSeance(), seance.getHeureDebut(), seance.getHeureFin(), seance.getStatut(), "INDIVIDUELLE");
    }

    /** Convertit une séance de groupe en ligne de synthèse (« qui » = nombre de confirmés). */
    private SeanceResumeVue resumeDe(SeanceGroupeVue seance) {
        return new SeanceResumeVue(seance.getIdSeance(),
                "Groupe (" + seance.getNombreConfirmes() + "/" + seance.getNombreInvites() + ")",
                seance.getNomMatiere(), seance.getDateSeance(), seance.getHeureDebut(), seance.getHeureFin(),
                seance.getStatut(), "GROUPE");
    }

    /** Met à jour les 4 cartes de synthèse (les valeurs suivent toujours le dernier rafraîchissement). */
    private void majStatistiques(List<DemandeRecueVue> demandes, List<DemandeSeanceRecueVue> demandesSeance,
                                  List<SeanceResumeVue> prochaines, List<SeanceResumeVue> terminees,
                                  List<EvaluationVue> evaluations) {
        long demandesEnAttente = demandes.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();
        long demandesSeanceEnAttente = demandesSeance.stream().filter(d -> "EN_ATTENTE".equals(d.getStatut())).count();

        statDemandesValue.setText(String.valueOf(demandes.size()));
        statDemandesDetail.setText(demandesEnAttente > 0
                ? demandesEnAttente + " en attente"
                : (demandesSeanceEnAttente > 0 ? demandesSeanceEnAttente + " demande(s) de séance en attente"
                : "aucune demande en attente"));

        statProchainesValue.setText(String.valueOf(prochaines.size()));
        statProchainesDetail.setText(prochaines.isEmpty()
                ? "aucune séance planifiée"
                : "prochaine le " + formatDate(prochaines.get(0).getDateSeance()));

        statTermineesValue.setText(String.valueOf(terminees.size()));
        statTermineesDetail.setText(terminees.isEmpty() ? "aucune séance réalisée" : "séances réalisées");

        double moyenne = evaluations.stream().mapToInt(EvaluationVue::getNote).average().orElse(0);
        statEvaluationsValue.setText(String.valueOf(evaluations.size()));
        statEvaluationsDetail.setText(evaluations.isEmpty()
                ? "aucune évaluation"
                : String.format(Locale.FRANCE, "moyenne %.1f/5", moyenne));

        evaluationsResumeLabel.setText(evaluations.isEmpty()
                ? "Aucune évaluation reçue pour le moment."
                : evaluations.size() + " évaluation(s) reçue(s) — moyenne "
                + String.format(Locale.FRANCE, "%.1f/5", moyenne));
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Renvoie la ligne sélectionnée, mais UNIQUEMENT si une seule ligne est sélectionnée :
     * les actions unitaires (accepter, refuser, planifier, annuler) portent sur une demande
     * précise, il ne faut donc jamais agir par surprise sur une ligne tirée d'une sélection
     * multiple. Renvoie null si 0 ou plusieurs lignes sont sélectionnées.
     */
    private static <T> T selectionUnique(TableView<T> table) {
        List<T> selection = new ArrayList<>(table.getSelectionModel().getSelectedItems());
        return selection.size() == 1 ? selection.get(0) : null;
    }

    /**
     * Active les boutons d'action selon la ligne sélectionnée : une demande (de mentorat ou de
     * séance) ne peut être traitée qu'en attente, une séance ne peut être modifiée que si elle est
     * planifiée. Sans sélection (ou avec plusieurs lignes), tout reste désactivé.
     */
    private void majEtatBoutons() {
        DemandeRecueVue demande = selectionUnique(demandesTable);
        boolean demandeEnAttente = demande != null && "EN_ATTENTE".equals(demande.getStatut());
        accepterDemandeButton.setDisable(!demandeEnAttente);
        refuserDemandeButton.setDisable(!demandeEnAttente);

        DemandeSeanceRecueVue demandeSeance = selectionUnique(demandeSeanceTable);
        boolean demandeSeanceEnAttente = demandeSeance != null && "EN_ATTENTE".equals(demandeSeance.getStatut());
        accepterSeanceButton.setDisable(!demandeSeanceEnAttente);
        refuserSeanceButton.setDisable(!demandeSeanceEnAttente);

        SeanceMentorVue seance = selectionUnique(seancesTable);
        boolean seancePlanifiee = seance != null && "PLANIFIEE".equals(seance.getStatut());
        marquerRealiseeButton.setDisable(!seancePlanifiee);
        annulerSeanceButton.setDisable(!seancePlanifiee);

        SeanceGroupeVue seanceGroupe = selectionUnique(seancesGroupeTable);
        boolean groupePlanifie = seanceGroupe != null && "PLANIFIEE".equals(seanceGroupe.getStatut());
        marquerGroupeRealiseeButton.setDisable(!groupePlanifie);
        annulerGroupeButton.setDisable(!groupePlanifie);
    }

    @FXML
    private void onNotificationsClick() {
        try {
            List<Notification> notifications = notificationDao.findNonLuesByUtilisateur(etudiantConnecte.getIdUtilisateur());
            StringBuilder details = new StringBuilder();
            for (Notification n : notifications) {
                details.append("• ").append(n.getMessage()).append("\n");
            }
            messageLabel.setText(details.toString());
            notificationDao.marquerToutesLuesPourUtilisateur(etudiantConnecte.getIdUtilisateur());
            notificationsButton.setVisible(false);
            notificationsButton.setManaged(false);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du chargement des notifications.");
        }
    }

    @FXML
    private void onAccepterClick() {
        DemandeRecueVue demande = selectionUnique(demandesTable);
        if (demande == null) {
            messageLabel.setText("Sélectionne une seule demande de mentorat dans le tableau.");
            return;
        }
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            messageLabel.setText("Cette demande a déjà été traitée.");
            return;
        }

        try {
            mentoratService.accepterDemande(demande.getIdDemande());
            messageLabel.setText("Demande de mentorat acceptée. L'étudiant peut maintenant demander une séance.");
            rafraichir();
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'acceptation.");
        }
    }

    @FXML
    private void onRefuserClick() {
        DemandeRecueVue demande = selectionUnique(demandesTable);
        if (demande == null) {
            messageLabel.setText("Sélectionne une seule demande de mentorat dans le tableau.");
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
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du refus.");
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

    /** Planifie la séance (date/heure/modalité/lieu saisis ci-dessous) pour la demande de séance sélectionnée. */
    @FXML
    private void onPlanifierSeanceClick() {
        DemandeSeanceRecueVue demandeSeance = selectionUnique(demandeSeanceTable);
        if (demandeSeance == null) {
            messageLabel.setText("Sélectionne une seule demande de séance dans le tableau.");
            return;
        }
        if (!"EN_ATTENTE".equals(demandeSeance.getStatut())) {
            messageLabel.setText("Cette demande de séance a déjà été traitée.");
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

            mentoratService.accepterDemandeSeance(demandeSeance.getIdDemandeSeance(), dateSeancePicker.getValue(),
                    heureDebut, heureFin, modalite, lieu);
            messageLabel.setText("Séance planifiée pour " + demandeSeance.getNomEtudiant() + ".");
            rafraichir();
        } catch (java.time.format.DateTimeParseException e) {
            messageLabel.setText("Format d'heure invalide — utilise HH:mm (ex: 14:30).");
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la planification.");
        }
    }

    @FXML
    private void onRefuserSeanceClick() {
        DemandeSeanceRecueVue demandeSeance = selectionUnique(demandeSeanceTable);
        if (demandeSeance == null) {
            messageLabel.setText("Sélectionne une seule demande de séance dans le tableau.");
            return;
        }
        if (!"EN_ATTENTE".equals(demandeSeance.getStatut())) {
            messageLabel.setText("Cette demande de séance a déjà été traitée.");
            return;
        }

        try {
            mentoratService.refuserDemandeSeance(demandeSeance.getIdDemandeSeance());
            messageLabel.setText("Demande de séance refusée.");
            rafraichir();
        } catch (IllegalStateException | IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du refus.");
        }
    }

    @FXML
    private void onEffacerDemandeSeanceClick() {
        List<DemandeSeanceRecueVue> selection = new ArrayList<>(demandeSeanceTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            messageLabel.setText("Sélectionne au moins une demande de séance à effacer.");
            return;
        }
        try {
            for (DemandeSeanceRecueVue demandeSeance : selection) {
                demandeSeanceDao.masquerPourMentor(demandeSeance.getIdDemandeSeance());
            }
            messageLabel.setText(selection.size() + " demande(s) de séance effacée(s) de ta vue.");
            rafraichir();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onMarquerRealiseeClick() {
        SeanceMentorVue seance = selectionUnique(seancesTable);
        if (seance == null) {
            messageLabel.setText("Sélectionne une seule séance dans le tableau.");
            return;
        }
        if (!"PLANIFIEE".equals(seance.getStatut())) {
            messageLabel.setText("Seule une séance planifiée peut être marquée comme réalisée.");
            return;
        }

        try {
            mentoratService.marquerSeanceRealisee(seance.getIdSeance());
            messageLabel.setText("Séance marquée comme réalisée.");
            rafraichir();
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void onAnnulerClick() {
        SeanceMentorVue seance = selectionUnique(seancesTable);
        if (seance == null) {
            messageLabel.setText("Sélectionne une seule séance dans le tableau.");
            return;
        }
        if (!"PLANIFIEE".equals(seance.getStatut())) {
            messageLabel.setText("Seule une séance planifiée peut être annulée.");
            return;
        }

        try {
            mentoratService.annulerSeance(seance.getIdSeance());
            messageLabel.setText("Séance annulée.");
            rafraichir();
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
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

    /** Recharge la liste "Partager avec" pour la matière choisie : "Tous les étudiants" + chaque étudiant accepté sur cette matière. */
    private void rafraichirDestinataires(Matiere matiere) {
        destinataireFichierComboBox.getItems().clear();
        destinataireFichierComboBox.getItems().add(new DestinataireVue(null, "Tous les étudiants"));
        if (matiere == null || mentorConnecte == null) {
            destinataireFichierComboBox.getSelectionModel().selectFirst();
            return;
        }
        try {
            destinataireFichierComboBox.getItems().addAll(
                    demandeDao.findDestinatairesAcceptesAvecNoms(mentorConnecte.getIdMentor(), matiere.getIdMatiere()));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des destinataires.");
        }
        destinataireFichierComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void onUploaderClick() {
        Matiere matiere = matiereFichierComboBox.getValue();
        DestinataireVue destinataire = destinataireFichierComboBox.getValue();
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

            Integer idEtudiantDestinataire = (destinataire == null) ? null : destinataire.getIdEtudiant();
            Fichier fichier = new Fichier(0, mentorConnecte.getIdMentor(), matiere.getIdMatiere(), null,
                    idEtudiantDestinataire, fichierChoisi.getName(), destination.toString(), LocalDate.now());
            fichierDao.create(fichier);

            String cible = idEtudiantDestinataire == null ? "tous les étudiants" : destinataire.toString();
            messageLabel.setText("Fichier « " + fichierChoisi.getName() + " » partagé avec " + cible + ".");
            fichierChoisi = null;
            fichierSelectionneLabel.setText("Aucun fichier choisi");
            rafraichir();
        } catch (java.io.IOException e) {
            messageLabel.setText("Erreur lors de la copie du fichier.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'enregistrement en base.");
        }
    }
    /** Barre latérale : affiche la section dont l'id est stocké dans le userData du bouton cliqué. */
    @FXML
    private void onMenuClick(ActionEvent event) {
        ToggleButton bouton = (ToggleButton) event.getSource();
        bouton.setSelected(true); // empêche de désélectionner la rubrique courante
        String cible = (String) bouton.getUserData();
        for (Node section : sectionsPane.getChildren()) {
            boolean visible = cible.equals(section.getId());
            section.setVisible(visible);
            section.setManaged(visible);
        }
        scrollContenu.setVvalue(0);
    }

    @FXML
    private void onHamburgerClick() {
        if (sidebarPane != null) {
            sidebarPane.setVisible(!sidebarPane.isVisible());
            sidebarPane.setManaged(sidebarPane.isVisible());
            Tooltip infobulle = hamburgerButton.getTooltip();
            if (infobulle != null) {
                infobulle.setText(sidebarPane.isVisible() ? "Masquer le menu" : "Afficher le menu");
            }
        }
    }

    @FXML
    private void onProfilClick() {
        try {
            Stage stage = (Stage) profilButton.getScene().getWindow();
            ProfilController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/profil.fxml", "Mentor-UADB - Mon profil");
            controller.setContexte(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir le profil.");
        }
    }

    @FXML
    private void onConsulterDemandeClick() {
        DemandeRecueVue demande = selectionUnique(demandesTable);
        if (demande == null) {
            messageLabel.setText("Sélectionne une seule demande dans le tableau.");
            return;
        }
        messageLabel.setText("Demande de " + demande.getNomEtudiant() + " pour " + demande.getNomMatiere()
                + " — demandée le " + demande.getDateDemande() + " (statut : " + demande.getStatut() + ").");
    }

    @FXML
    private void onConsulterDemandeSeanceClick() {
        DemandeSeanceRecueVue demandeSeance = selectionUnique(demandeSeanceTable);
        if (demandeSeance == null) {
            messageLabel.setText("Sélectionne une seule demande de séance dans le tableau.");
            return;
        }
        StringBuilder details = new StringBuilder("Demande de " + demandeSeance.getNomEtudiant() + " pour "
                + demandeSeance.getNomMatiere() + " — créneau souhaité : "
                + (demandeSeance.getDateSouhaitee() == null ? "au premier moment possible" : demandeSeance.getDateSouhaitee())
                + " (statut : " + demandeSeance.getStatut() + ").");
        if (demandeSeance.getMessage() != null && !demandeSeance.getMessage().isBlank()) {
            details.append(" Message : ").append(demandeSeance.getMessage());
        }
        messageLabel.setText(details.toString());
    }

    @FXML
    private void onGroupeMarquerRealiseeClick() {
        SeanceGroupeVue seance = selectionUnique(seancesGroupeTable);
        if (seance == null) {
            messageLabel.setText("Sélectionne une seule séance de groupe dans le tableau.");
            return;
        }
        if (!"PLANIFIEE".equals(seance.getStatut())) {
            messageLabel.setText("Seule une séance planifiée peut être marquée comme réalisée.");
            return;
        }

        try {
            mentoratService.marquerSeanceRealisee(seance.getIdSeance());
            messageLabel.setText("Séance de groupe marquée comme réalisée.");
            rafraichir();
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void onGroupeAnnulerClick() {
        SeanceGroupeVue seance = selectionUnique(seancesGroupeTable);
        if (seance == null) {
            messageLabel.setText("Sélectionne une seule séance de groupe dans le tableau.");
            return;
        }
        if (!"PLANIFIEE".equals(seance.getStatut())) {
            messageLabel.setText("Seule une séance planifiée peut être annulée.");
            return;
        }

        try {
            mentoratService.annulerSeance(seance.getIdSeance());
            messageLabel.setText("Séance de groupe annulée.");
            rafraichir();
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'annulation.");
        }
    }

    @FXML
    private void onOuvrirFichierClick() {
        Fichier fichier = fichiersTable.getSelectionModel().getSelectedItem();
        if (fichier == null) {
            messageLabel.setText("Sélectionne un fichier dans le tableau d'abord.");
            return;
        }

        java.io.File f = new java.io.File(fichier.getChemin());
        if (!f.exists()) {
            messageLabel.setText("Fichier introuvable sur le disque.");
            return;
        }

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            messageLabel.setText(
                    "Ouverture automatique non disponible ici. Chemin du fichier : " + f.getAbsolutePath());
            return;
        }

        messageLabel.setText("Ouverture en cours...");
        Thread thread = new Thread(() -> {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> messageLabel.setText(
                        "Impossible d'ouvrir automatiquement. Chemin : " + f.getAbsolutePath()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onDeconnexionClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "Mentor-UADB - Connexion");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }

    @FXML
    private void onRetourEtudiantClick() {
        try {
            Stage stage = (Stage) retourEtudiantButton.getScene().getWindow();
            DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "Mentor-UADB - Tableau de bord"
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
                    stage, "/com/uadb/mentoruadb/fxml/seance-groupe.fxml", "Mentor-UADB - Séance de groupe"
            );
            controller.setContexte(etudiantConnecte, mentorConnecte, prenomConnecte);
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de séance de groupe.");
        }
    }
}
// ==================== FIN CLASSE ====================
