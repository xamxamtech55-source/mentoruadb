package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.dao.UtilisateurDao;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.util.DialogueUtil;
import com.uadb.mentoruadb.util.MessageLabel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/** Contrôleur lié à fxml/gestion-utilisateurs.fxml. Le mot de passe n'est jamais affiché (exigence sécurité). */
public class GestionUtilisateursController {

    @FXML private TableView<Utilisateur> utilisateursTable;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colStatut;
    @FXML private Label messageLabel;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> coloredStatutCell());
        MessageLabel.rendreDynamique(messageLabel);
        rafraichir();
    }

    /** Colore le statut de compte (ACTIF / INACTIF) pour le distinguer d'un coup d'œil. */
    private static TableCell<Utilisateur, String> coloredStatutCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean vide) {
                super.updateItem(item, vide);
                if (vide || item == null) {
                    setText(null);
                    getStyleClass().removeIf(c -> c.startsWith("statut-"));
                } else {
                    setText(item);
                    getStyleClass().removeIf(c -> c.startsWith("statut-"));
                    getStyleClass().add("ACTIF".equals(item) ? "statut-valide" : "statut-refuse");
                }
            }
        };
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
        changerStatut("ACTIF", null);
    }

    @FXML
    private void onDesactiverClick() {
        Utilisateur utilisateur = utilisateursTable.getSelectionModel().getSelectedItem();
        if (utilisateur == null) {
            messageLabel.setText("Sélectionne un utilisateur dans le tableau d'abord.");
            return;
        }
        boolean confirme = confirmer("Désactiver le compte",
                "Désactiver le compte de " + nomComplet(utilisateur) + " (" + utilisateur.getEmail() + ") ?\n"
                        + "Il ne pourra plus se connecter tant qu'il n'est pas réactivé.");
        if (confirme) {
            changerStatut("INACTIF", utilisateur);
        }
    }

    private void changerStatut(String statut, Utilisateur selection) {
        Utilisateur utilisateur = selection != null ? selection
                : utilisateursTable.getSelectionModel().getSelectedItem();
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
            succes("Statut mis à jour : " + nomComplet(utilisateur) + " est maintenant " + statut + ".");
        } catch (SQLException e) {
            messageLabel.getStyleClass().setAll("message-label");
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

        boolean confirme = confirmer("Supprimer le compte",
                "Supprimer définitivement le compte de " + nomComplet(utilisateur)
                        + " (" + utilisateur.getEmail() + ") ?\n"
                        + "Ses données liées (étudiant, mentor…) empêcheront la suppression.");
        if (!confirme) {
            return;
        }

        try {
            utilisateurDao.delete(utilisateur.getIdUtilisateur());
            rafraichir();
            succes("Utilisateur supprimé.");
        } catch (SQLException e) {
            messageLabel.getStyleClass().setAll("message-label");
            messageLabel.setText("Impossible de supprimer : cet utilisateur a des données liées (étudiant, mentor...).");
        }
    }

    private static boolean confirmer(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(null);
        DialogueUtil.contenu(alert, contenu);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static String nomComplet(Utilisateur utilisateur) {
        return (utilisateur.getPrenom() != null ? utilisateur.getPrenom() : "") + " "
                + (utilisateur.getNom() != null ? utilisateur.getNom() : "");
    }

    /** Le message de confirmation n'est pas une erreur : il passe en vert. */
    private void succes(String texte) {
        messageLabel.getStyleClass().setAll("succes-label");
        messageLabel.setText(texte);
    }
}
// ==================== FIN CLASSE ====================
