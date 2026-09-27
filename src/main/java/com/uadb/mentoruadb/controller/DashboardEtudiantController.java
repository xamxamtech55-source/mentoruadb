package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.FichierDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dao.SeanceParticipantDao;
import com.uadb.mentoruadb.dto.DemandeVue;
import com.uadb.mentoruadb.dto.SeanceVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Fichier;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Contrôleur lié à fxml/dashboard-etudiant.fxml. */
public class DashboardEtudiantController {

    private static final String NIVEAU_BLOQUE_MENTORAT = "Licence 1";

    @FXML private Label bienvenueLabel;
    @FXML private Button notificationButton;
    @FXML private Button rechercherMentorButton;
    @FXML private Button tableauMentorButton;
    @FXML private Button deconnexionButton;
    @FXML private Button devenirMentorButton;
    @FXML private Button profilButton;

    @FXML private TableView<SeanceVue> seancesAConfirmerTable;
    @FXML private TableColumn<SeanceVue, String> colConfirmMentor;
    @FXML private TableColumn<SeanceVue, String> colConfirmMatiere;
    @FXML private TableColumn<SeanceVue, String> colConfirmDate;
    @FXML private TableColumn<SeanceVue, String> colConfirmHeure;

    @FXML private TableView<DemandeVue> demandesTable;
    @FXML private TableColumn<DemandeVue, String> colDemandeMentor;
    @FXML private TableColumn<DemandeVue, String> colDemandeMatiere;
    @FXML private TableColumn<DemandeVue, String> colDemandeDate;
    @FXML private TableColumn<DemandeVue, String> colDemandeStatut;

    @FXML private TableView<SeanceVue> seancesTable;
    @FXML private TableColumn<SeanceVue, String> colSeanceMentor;
    @FXML private TableColumn<SeanceVue, String> colSeanceMatiere;
    @FXML private TableColumn<SeanceVue, String> colSeanceDate;
    @FXML private TableColumn<SeanceVue, String> colSeanceHeureDebut;
    @FXML private TableColumn<SeanceVue, String> colSeanceHeureFin;
    @FXML private TableColumn<SeanceVue, String> colSeanceStatut;

    @FXML private TableView<Fichier> fichiersTable;
    @FXML private TableColumn<Fichier, String> colFichierNom;
    @FXML private TableColumn<Fichier, String> colFichierMatiere;
    @FXML private TableColumn<Fichier, String> colFichierDate;

    @FXML private ComboBox<Integer> noteComboBox;
    @FXML private TextField commentaireField;
    @FXML private Label messageLabel;

    private final MentorDao mentorDao = new MentorDao();
    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final SeanceParticipantDao seanceParticipantDao = new SeanceParticipantDao();
    private final NiveauDao niveauDao = new NiveauDao();
    private final FichierDao fichierDao = new FichierDao();
    private final MatiereDao matiereDao = new MatiereDao();
    private final MentoratService mentoratService = new MentoratService();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;
    private boolean niveauBloquePourMentorat = false;
    private List<SeanceVue> seancesGroupeNonVues = new ArrayList<>();

    @FXML
    public void initialize() {
        colConfirmMentor.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colConfirmMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colConfirmDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colConfirmHeure.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));

        colDemandeMentor.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colDemandeMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colDemandeDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDemandeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        demandesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colSeanceMentor.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colSeanceMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colSeanceDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colSeanceHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colSeanceHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colSeanceStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        seancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colFichierNom.setCellValueFactory(new PropertyValueFactory<>("nomFichier"));
        colFichierDate.setCellValueFactory(new PropertyValueFactory<>("dateUpload"));
        colFichierMatiere.setCellValueFactory(cellData -> {
            try {
                Integer idMatiere = cellData.getValue().getIdMatiere();
                String nom = idMatiere == null ? "-" : matiereDao.findById(idMatiere).map(Matiere::getNom).orElse("?");
                return new javafx.beans.property.SimpleStringProperty(nom);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        noteComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
    }

    /** Appelée manuellement depuis LoginController juste après avoir chargé cet écran. */
    public void chargerDonnees(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;
        rafraichir(etudiant);
    }

    /** Recharge les tableaux, sans redemander le prénom (utile après un retour d'un autre écran). */
    public void rafraichir(Etudiant etudiant) {
        this.etudiantConnecte = etudiant;
        bienvenueLabel.setText("Bienvenue " + prenomConnecte);

        try {
            seancesAConfirmerTable.setItems(FXCollections.observableArrayList(
                    seanceDao.findGroupesAConfirmerByEtudiant(etudiant.getIdEtudiant())
            ));

            demandesTable.setItems(FXCollections.observableArrayList(
                    demandeDao.findByEtudiantAvecDetails(etudiant.getIdEtudiant())
            ));

            List<SeanceVue> toutesLesSeances = new ArrayList<>();
            toutesLesSeances.addAll(seanceDao.findByEtudiantAvecDetails(etudiant.getIdEtudiant()));
            toutesLesSeances.addAll(seanceDao.findGroupesByEtudiantAvecDetails(etudiant.getIdEtudiant()));
            seancesTable.setItems(FXCollections.observableArrayList(toutesLesSeances));

            fichiersTable.setItems(FXCollections.observableArrayList(
                    fichierDao.findVisiblesParEtudiant(etudiant.getIdEtudiant())
            ));

            seancesGroupeNonVues = seanceDao.findGroupesAVenirByEtudiant(etudiant.getIdEtudiant());
            if (!seancesGroupeNonVues.isEmpty()) {
                notificationButton.setText("🔔 " + seancesGroupeNonVues.size() + " nouvelle(s) séance(s) de groupe — clique pour voir");
                notificationButton.setVisible(true);
                notificationButton.setManaged(true);
            } else {
                notificationButton.setVisible(false);
                notificationButton.setManaged(false);
            }

            Optional<Niveau> niveauOpt = niveauDao.findById(etudiant.getIdNiveau());
            niveauBloquePourMentorat = niveauOpt.isPresent()
                    && NIVEAU_BLOQUE_MENTORAT.equalsIgnoreCase(niveauOpt.get().getLibelle());

            devenirMentorButton.setVisible(!niveauBloquePourMentorat);
            devenirMentorButton.setManaged(!niveauBloquePourMentorat);
            tableauMentorButton.setVisible(!niveauBloquePourMentorat);
            tableauMentorButton.setManaged(!niveauBloquePourMentorat);
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur de chargement des données.");
        }
    }

    @FXML
    private void onNotificationClick() {
        if (seancesGroupeNonVues.isEmpty()) return;

        StringBuilder details = new StringBuilder();
        for (SeanceVue s : seancesGroupeNonVues) {
            details.append("• ").append(s.getNomMatiere()).append(" avec ").append(s.getNomMentor())
                    .append(" — ").append(s.getDateSeance()).append(" de ").append(s.getHeureDebut())
                    .append(" à ").append(s.getHeureFin()).append(" (").append(s.getStatut()).append(")\n");
        }
        messageLabel.setText(details.toString());

        try {
            for (SeanceVue s : seancesGroupeNonVues) {
                seanceParticipantDao.marquerVu(s.getIdSeance(), etudiantConnecte.getIdEtudiant());
            }
        } catch (SQLException e) {
            messageLabel.setText(messageLabel.getText() + "\n(Erreur lors du marquage comme vu.)");
        }

        notificationButton.setVisible(false);
        notificationButton.setManaged(false);
        seancesGroupeNonVues.clear();
    }

    @FXML
    private void onConfirmerPresenceClick() {
        SeanceVue seance = seancesAConfirmerTable.getSelectionModel().getSelectedItem();
        if (seance == null) {
            messageLabel.setText("Sélectionne une séance à confirmer d'abord.");
            return;
        }

        try {
            seanceParticipantDao.confirmerPresence(seance.getIdSeance(), etudiantConnecte.getIdEtudiant());
            messageLabel.setText("Présence confirmée pour la séance du " + seance.getDateSeance() + ".");
            rafraichir(etudiantConnecte);
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la confirmation.");
        }
    }

    @FXML
    private void onEffacerDemandeClick() {
        List<DemandeVue> selection = new ArrayList<>(demandesTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            bienvenueLabel.setText("Sélectionne au moins une demande à effacer.");
            return;
        }
        try {
            for (DemandeVue demande : selection) {
                demandeDao.masquerPourEtudiant(demande.getIdDemande());
            }
            bienvenueLabel.setText(selection.size() + " demande(s) effacée(s) de ta vue.");
            rafraichir(etudiantConnecte);
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onEffacerSeanceClick() {
        List<SeanceVue> selection = new ArrayList<>(seancesTable.getSelectionModel().getSelectedItems());
        if (selection.isEmpty()) {
            bienvenueLabel.setText("Sélectionne au moins une séance à effacer.");
            return;
        }
        try {
            for (SeanceVue seance : selection) {
                if ("GROUPE".equals(seance.getTypeSeance())) {
                    seanceParticipantDao.masquerPourEtudiant(seance.getIdSeance(), etudiantConnecte.getIdEtudiant());
                } else {
                    seanceDao.masquerIndividuellePourEtudiant(seance.getIdSeance());
                }
            }
            bienvenueLabel.setText(selection.size() + " séance(s) effacée(s) de ta vue.");
            rafraichir(etudiantConnecte);
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur lors de l'effacement.");
        }
    }

    @FXML
    private void onRechercherMentorClick() {
        try {
            Stage stage = (Stage) rechercherMentorButton.getScene().getWindow();
            RechercheMentorController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/recherche-mentor.fxml", "MentorUADB - Recherche de mentor"
            );
            controller.setEtudiantConnecte(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            bienvenueLabel.setText("Impossible d'ouvrir la recherche de mentor.");
        }
    }

    @FXML
    private void onTableauMentorClick() {
        if (niveauBloquePourMentorat) {
            bienvenueLabel.setText("Le mentorat n'est pas disponible en " + NIVEAU_BLOQUE_MENTORAT + ".");
            return;
        }
        try {
            Optional<Mentor> mentorOpt = mentorDao.findByEtudiant(etudiantConnecte.getIdEtudiant());
            if (mentorOpt.isEmpty()) {
                bienvenueLabel.setText("Tu n'as pas encore de profil mentor. Utilise \"Devenir mentor\" d'abord.");
                return;
            }
            if (!"VALIDE".equals(mentorOpt.get().getStatutValidation())) {
                bienvenueLabel.setText("Ta candidature mentor n'est pas encore validée (statut : "
                        + mentorOpt.get().getStatutValidation() + ").");
                return;
            }

            Stage stage = (Stage) tableauMentorButton.getScene().getWindow();
            DashboardMentorController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/dashboard-mentor.fxml", "MentorUADB - Espace mentor"
            );
            controller.chargerDonnees(etudiantConnecte, mentorOpt.get(), prenomConnecte);
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur lors de l'accès à l'espace mentor.");
        } catch (IOException e) {
            bienvenueLabel.setText("Impossible d'ouvrir le tableau de bord mentor.");
        }
    }

    @FXML
    private void onDeconnexionClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login.fxml", "MentorUADB - Connexion");
        } catch (IOException e) {
            bienvenueLabel.setText("Impossible de revenir à l'écran de connexion.");
        }
    }

    @FXML
    private void onDevenirMentorClick() {
        if (niveauBloquePourMentorat) {
            bienvenueLabel.setText("Le mentorat n'est pas disponible en " + NIVEAU_BLOQUE_MENTORAT + ".");
            return;
        }
        try {
            Stage stage = (Stage) devenirMentorButton.getScene().getWindow();
            DevenirMentorController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/devenir-mentor.fxml", "MentorUADB - Devenir mentor"
            );
            controller.setEtudiantConnecte(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            bienvenueLabel.setText("Impossible d'ouvrir l'écran de candidature mentor.");
        }
    }

    @FXML
    private void onProfilClick() {
        try {
            Stage stage = (Stage) profilButton.getScene().getWindow();
            ProfilController controller = SceneNavigator.switchToAndGetController(
                    stage, "/com/uadb/mentoruadb/fxml/profil.fxml", "MentorUADB - Mon profil"
            );
            controller.setContexte(etudiantConnecte, prenomConnecte);
        } catch (IOException e) {
            bienvenueLabel.setText("Impossible d'ouvrir le profil.");
        }
    }

    @FXML
    private void onOuvrirFichierClick() {
        Fichier fichier = fichiersTable.getSelectionModel().getSelectedItem();
        if (fichier == null) {
            bienvenueLabel.setText("Sélectionne un fichier dans le tableau d'abord.");
            return;
        }

        File f = new File(fichier.getChemin());
        if (!f.exists()) {
            bienvenueLabel.setText("Fichier introuvable sur le disque.");
            return;
        }

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            bienvenueLabel.setText("Ouverture automatique non disponible ici. Chemin du fichier : " + f.getAbsolutePath());
            return;
        }

        bienvenueLabel.setText("Ouverture en cours...");
        Thread thread = new Thread(() -> {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        bienvenueLabel.setText("Impossible d'ouvrir automatiquement. Chemin : " + f.getAbsolutePath())
                );
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEvaluerClick() {
        SeanceVue seance = seancesTable.getSelectionModel().getSelectedItem();
        Integer note = noteComboBox.getValue();

        if (seance == null) {
            bienvenueLabel.setText("Sélectionne une séance dans le tableau d'abord.");
            return;
        }
        if (note == null) {
            bienvenueLabel.setText("Choisis une note entre 1 et 5.");
            return;
        }

        try {
            mentoratService.evaluerSeance(seance.getIdSeance(), etudiantConnecte.getIdEtudiant(), note, commentaireField.getText());
            bienvenueLabel.setText("Évaluation enregistrée, merci !");
            commentaireField.clear();
            noteComboBox.setValue(null);
        } catch (IllegalStateException | IllegalArgumentException e) {
            bienvenueLabel.setText(e.getMessage());
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur lors de l'enregistrement de l'évaluation.");
        }
    }
}