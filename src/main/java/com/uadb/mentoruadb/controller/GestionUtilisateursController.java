package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/** Contrôleur lié à fxml/gestion-utilisateurs.fxml. Le mot de passe n'est jamais affiché (exigence sécurité). */
public class GestionUtilisateursController {

    @FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colStatut;
    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        rafraichir();
    }

    private void rafraichir() {
        try {
            utilisateursTable.setItems(FXCollections.observableArrayList(utilisateurDao.findAll()));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des utilisateurs.");
        }
    }

    @FXML
    private void onActiverClick() {
        changerStatut("ACTIF");
    }

    @FXML
    private void onDesactiverClick() {
        changerStatut("INACTIF");
    }

    private void changerStatut(String statut) {
        Utilisateur utilisateur = utilisateursTable.getSelectionModel().getSelectedItem();
        if (utilisateur == null) {
            messageLabel.setText("Sélectionne un utilisateur dans le tableau d'abord.");
            return;
        }
        if ("ADMIN".equals(utilisateur.getRole()) && "INACTIF".equals(statut)) {
            messageLabel.setText("Impossible de désactiver un compte administrateur.");
            return;
        }

        try {
            utilisateur.setStatut(statut);
            utilisateurDao.update(utilisateur);
            rafraichir();
            messageLabel.setText("Statut mis à jour : " + statut + ".");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de la mise à jour.");
        }
    }

    @FXML
    private void onSupprimerClick() {
        Utilisateur utilisateur = utilisateursTable.getSelectionModel().getSelectedItem();
        if (utilisateur == null) {
            messageLabel.setText("Sélectionne un utilisateur dans le tableau d'abord.");
            return;
        }
        if ("ADMIN".equals(utilisateur.getRole())) {
            messageLabel.setText("Impossible de supprimer un compte administrateur.");
            return;
        }

        try {
            utilisateurDao.delete(utilisateur.getIdUtilisateur());
            rafraichir();
            messageLabel.setText("Utilisateur supprimé.");
        } catch (SQLException e) {
            messageLabel.setText("Impossible de supprimer : cet utilisateur a des données liées (étudiant, mentor...).");
        }
    }

    @FXML
    private void onRetourClick() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "MentorUADB - Administration");
        } catch (IOException e) {
            messageLabel.setText("Impossible de revenir au tableau de bord admin.");
        }
    }
}