package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.EvaluationDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dto.DemandeVue;
import com.uadb.mentoruadb.dto.SeanceVue;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Mentor;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.service.MentoratService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.List;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import com.uadb.mentoruadb.dao.FichierDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.model.Fichier;
import javafx.scene.control.TableColumn;
import java.awt.Desktop;
import java.io.File;
import com.uadb.mentoruadb.model.Matiere;

/** Contrôleur lié à fxml/dashboard-etudiant.fxml. */
public class DashboardEtudiantController {

    private static final String NIVEAU_BLOQUE_MENTORAT = "Licence 1";

    @FXML private Label bienvenueLabel;

    @FXML private TableView<DemandeVue> demandesTable;
    @FXML private TableColumn<DemandeVue, String> colDemandeMentor;
    @FXML private TableColumn<DemandeVue, String> colDemandeMatiere;
    @FXML private TableColumn<DemandeVue, String> colDemandeDate;
    @FXML private TableColumn<DemandeVue, String> colDemandeStatut;

    @FXML private ComboBox<Integer> noteComboBox;
    @FXML private TextField commentaireField;

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

    @FXML private Button rechercherMentorButton;
    @FXML private Button tableauMentorButton;
    @FXML private Button deconnexionButton;
    @FXML private Button devenirMentorButton;
    @FXML private Label notificationLabel;
    @FXML private Button profilButton;

    private final MentorDao mentorDao = new MentorDao();
    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final SeanceDao seanceDao = new SeanceDao();
    private final EvaluationDao evaluationDao = new EvaluationDao();
    private final NiveauDao niveauDao = new NiveauDao();

    private final FichierDao fichierDao = new FichierDao();
    private final MatiereDao matiereDao = new MatiereDao();

    private Etudiant etudiantConnecte;
    private String prenomConnecte;
    private boolean niveauBloquePourMentorat = false;

    /** Configure les colonnes. Appelée automatiquement par JavaFX après le chargement du FXML. */
    @FXML
    public void initialize() {
        colDemandeMentor.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colDemandeMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colDemandeDate.setCellValueFactory(new PropertyValueFactory<>("dateDemande"));
        colDemandeStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colSeanceMentor.setCellValueFactory(new PropertyValueFactory<>("nomMentor"));
        colSeanceMatiere.setCellValueFactory(new PropertyValueFactory<>("nomMatiere"));
        colSeanceDate.setCellValueFactory(new PropertyValueFactory<>("dateSeance"));
        colSeanceHeureDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        colSeanceHeureFin.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        colSeanceStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        noteComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));

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
    }

    /**
     * Appelée manuellement depuis LoginController juste après avoir chargé cet écran,
     * pour lui transmettre l'étudiant connecté (le FXMLLoader ne le fait pas tout seul).
     */
    public void chargerDonnees(Etudiant etudiant, String prenom) {
        this.etudiantConnecte = etudiant;
        this.prenomConnecte = prenom;
        rafraichir(etudiant);
    }

    /** Recharge juste les tableaux, sans redemander le prénom (utile après un retour d'un autre écran). */
    public void rafraichir(Etudiant etudiant) {
        this.etudiantConnecte = etudiant;
        bienvenueLabel.setText("Bienvenue " + prenomConnecte);

        try {
            demandesTable.setItems(FXCollections.observableArrayList(
                    demandeDao.findByEtudiantAvecDetails(etudiant.getIdEtudiant())
            ));
            java.util.List<SeanceVue> toutesLesSeances = new java.util.ArrayList<>();
            toutesLesSeances.addAll(seanceDao.findByEtudiantAvecDetails(etudiant.getIdEtudiant()));
            toutesLesSeances.addAll(seanceDao.findGroupesByEtudiantAvecDetails(etudiant.getIdEtudiant()));
            seancesTable.setItems(FXCollections.observableArrayList(toutesLesSeances));
            List<SeanceVue> seancesGroupeAVenir = seanceDao.findGroupesAVenirByEtudiant(etudiant.getIdEtudiant());
            if (!seancesGroupeAVenir.isEmpty()) {
                StringBuilder notif = new StringBuilder("🔔 Nouvelle(s) séance(s) de groupe : ");
                for (int i = 0; i < seancesGroupeAVenir.size(); i++) {
                    SeanceVue s = seancesGroupeAVenir.get(i);
                    notif.append(s.getNomMatiere()).append(" avec ").append(s.getNomMentor())
                            .append(" le ").append(s.getDateSeance());
                    if (i < seancesGroupeAVenir.size() - 1) notif.append(" ; ");
                }
                notificationLabel.setText(notif.toString());
                notificationLabel.setVisible(true);
                notificationLabel.setManaged(true);
            } else {
                notificationLabel.setVisible(false);
                notificationLabel.setManaged(false);
            }
            Optional<Niveau> niveauOpt = niveauDao.findById(etudiant.getIdNiveau());
            niveauBloquePourMentorat = niveauOpt.isPresent()
                    && NIVEAU_BLOQUE_MENTORAT.equalsIgnoreCase(niveauOpt.get().getLibelle());

            devenirMentorButton.setVisible(!niveauBloquePourMentorat);
            devenirMentorButton.setManaged(!niveauBloquePourMentorat);
            tableauMentorButton.setVisible(!niveauBloquePourMentorat);
            tableauMentorButton.setManaged(!niveauBloquePourMentorat);
            fichiersTable.setItems(FXCollections.observableArrayList(fichierDao.findVisiblesParEtudiant(etudiant.getIdEtudiant())));
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur de chargement des données.");
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
            new MentoratService().evaluerSeance(seance.getIdSeance(), note, commentaireField.getText());
            bienvenueLabel.setText("Évaluation enregistrée, merci !");
            commentaireField.clear();
            noteComboBox.setValue(null);
        } catch (IllegalStateException | IllegalArgumentException e) {
            bienvenueLabel.setText(e.getMessage());
        } catch (SQLException e) {
            bienvenueLabel.setText("Erreur lors de l'enregistrement de l'évaluation.");
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
}