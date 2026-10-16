package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.DemandeMentoratDao;
import com.uadb.mentoruadb.dao.MentorDao;
import com.uadb.mentoruadb.dao.SeanceDao;
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.DemandeMentorat;
import com.uadb.mentoruadb.model.Seance;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Contrôleur lié à fxml/dashboard-admin.fxml. */
public class DashboardAdminController {

    @FXML
    private Label bienvenueLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Button deconnexionButton;
    @FXML
    private StackPane sectionsPane;
    @FXML
    private Label statEtudiants;
    @FXML
    private Label statMentors;
    @FXML
    private Label statCandidatures;
    @FXML
    private Label statSeancesAVenir;
    @FXML
    private VBox demandesBox;
    @FXML
    private VBox seancesBox;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private final MentorDao mentorDao = new MentorDao();
    private final DemandeMentoratDao demandeDao = new DemandeMentoratDao();
    private final SeanceDao seanceDao = new SeanceDao();

    public void chargerDonnees(String prenom) {
        bienvenueLabel.setText("Administration — " + prenom);
        majStatistiques();
    }

    /**
     * Barre latérale : affiche la section dont l'id est stocké dans le userData du
     * bouton cliqué.
     */
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
        if ("secAccueil".equals(cible)) {
            majStatistiques(); // les chiffres ont pu changer depuis les autres rubriques
        }
    }

    /**
     * Calcule les statistiques de l'accueil à partir des DAO existants (aucune
     * nouvelle requête SQL).
     */
    private void majStatistiques() {
        try {
            List<Utilisateur> utilisateurs = utilisateurDao.findAll();
            long etudiants = utilisateurs.stream().filter(u -> "ETUDIANT".equals(u.getRole())).count();
            long mentorsValides = mentorDao.findByStatut("VALIDE").size();
            long candidatures = mentorDao.findByStatut("EN_ATTENTE").size();

            List<DemandeMentorat> demandes = demandeDao.findAll();
            List<Seance> seances = seanceDao.findAll();
            LocalDate aujourdhui = LocalDate.now();
            long seancesAVenir = seances.stream()
                    .filter(s -> "PLANIFIEE".equals(s.getStatut())
                            && s.getDateSeance() != null && !s.getDateSeance().isBefore(aujourdhui))
                    .count();

            statEtudiants.setText(String.valueOf(etudiants));
            statMentors.setText(String.valueOf(mentorsValides));
            statCandidatures.setText(String.valueOf(candidatures));
            statSeancesAVenir.setText(String.valueOf(seancesAVenir));

            demandesBox.getChildren().clear();
            long totalDemandes = demandes.size();
            ajouterBarre(demandesBox, "En attente", compter(demandes, "EN_ATTENTE"), totalDemandes);
            ajouterBarre(demandesBox, "Acceptées", compter(demandes, "ACCEPTEE"), totalDemandes);
            ajouterBarre(demandesBox, "Refusées", compter(demandes, "REFUSEE"), totalDemandes);

            seancesBox.getChildren().clear();
            long totalSeances = seances.size();
            ajouterBarre(seancesBox, "Planifiées",
                    seances.stream().filter(s -> "PLANIFIEE".equals(s.getStatut())).count(), totalSeances);
            ajouterBarre(seancesBox, "Réalisées",
                    seances.stream().filter(s -> "REALISEE".equals(s.getStatut())).count(), totalSeances);
            ajouterBarre(seancesBox, "Annulées", seances.stream().filter(s -> "ANNULEE".equals(s.getStatut())).count(),
                    totalSeances);
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des statistiques.");
        }
    }

    private long compter(List<DemandeMentorat> demandes, String statut) {
        return demandes.stream().filter(d -> statut.equals(d.getStatut())).count();
    }

    /** Une ligne : libellé, barre de progression (part du total) et valeur. */
    private void ajouterBarre(VBox conteneur, String libelle, long valeur, long total) {
        Label nom = new Label(libelle);
        nom.setMinWidth(110);

        ProgressBar barre = new ProgressBar(total == 0 ? 0 : (double) valeur / total);
        barre.setPrefWidth(320);
        barre.getStyleClass().add("barre-stat");

        Label nombre = new Label(String.valueOf(valeur));
        nombre.getStyleClass().add("seance-date");

        HBox ligne = new HBox(12, nom, barre, nombre);
        ligne.setStyle("-fx-alignment: CENTER_LEFT;");
        conteneur.getChildren().add(ligne);
    }

    @FXML
    private void onValiderMentorsClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/validation-mentors.fxml",
                    "Mentor-UADB - Validation des mentors");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran de validation.");
        }
    }

    @FXML
    private void onGestionDonneesClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/gestion-donnees.fxml",
                    "Mentor-UADB - Gestion des données");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }

    @FXML
    private void onListeMentorsClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/liste-mentors.fxml",
                    "Mentor-UADB - Liste des mentors");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
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
    private void onGestionUtilisateursClick() {
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/gestion-utilisateurs.fxml",
                    "Mentor-UADB - Gestion des utilisateurs");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}