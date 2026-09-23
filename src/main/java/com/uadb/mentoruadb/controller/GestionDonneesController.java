package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.MatiereDao;
import com.uadb.mentoruadb.dao.NiveauDao;
import com.uadb.mentoruadb.dao.UfrDao;
import com.uadb.mentoruadb.model.Filiere;
import com.uadb.mentoruadb.model.Matiere;
import com.uadb.mentoruadb.model.Niveau;
import com.uadb.mentoruadb.model.Ufr;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/** Contrôleur lié à fxml/gestion-donnees.fxml. */
public class GestionDonneesController {

    // --- UFR ---
    @FXML private TableView<Ufr> ufrTable;
    @FXML private TableColumn<Ufr, String> colUfrNom;
    @FXML private TextField ufrNomField;

    // --- Filiere ---
    @FXML private TableView<Filiere> filiereTable;
    @FXML private TableColumn<Filiere, String> colFiliereNom;
    @FXML private TableColumn<Filiere, String> colFiliereUfr;
    @FXML private TextField filiereNomField;
    @FXML private ComboBox<Ufr> filiereUfrComboBox;

    // --- Niveau ---
    @FXML private TableView<Niveau> niveauTable;
    @FXML private TableColumn<Niveau, String> colNiveauLibelle;
    @FXML private TextField niveauLibelleField;

    // --- Matiere ---
    @FXML private TableView<Matiere> matiereTable;
    @FXML private TableColumn<Matiere, String> colMatiereNom;
    @FXML private TextField matiereNomField;

    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UfrDao ufrDao = new UfrDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final NiveauDao niveauDao = new NiveauDao();
    private final MatiereDao matiereDao = new MatiereDao();

    @FXML
    public void initialize() {
        colUfrNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveauLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        colMatiereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        colFiliereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colFiliereUfr.setCellValueFactory(cellData -> {
            try {
                int idUfr = cellData.getValue().getIdUfr();
                String nomUfr = ufrDao.findById(idUfr).map(Ufr::getNom).orElse("?");
                return new javafx.beans.property.SimpleStringProperty(nomUfr);
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        rafraichirTout();
    }

    private void rafraichirTout() {
        try {
            ufrTable.setItems(FXCollections.observableArrayList(ufrDao.findAll()));
            filiereUfrComboBox.setItems(FXCollections.observableArrayList(ufrDao.findAll()));
            filiereTable.setItems(FXCollections.observableArrayList(filiereDao.findAll()));
            niveauTable.setItems(FXCollections.observableArrayList(niveauDao.findAll()));
            matiereTable.setItems(FXCollections.observableArrayList(matiereDao.findAll()));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des données.");
        }
    }

    // --- UFR ---
    @FXML
    private void onAjouterUfrClick() {
        String nom = ufrNomField.getText();
        if (nom.isBlank()) {
            messageLabel.setText("Renseigne un nom d'UFR.");
            return;
        }
        try {
            ufrDao.create(new Ufr(0, nom));
            ufrNomField.clear();
            rafraichirTout();
            messageLabel.setText("UFR ajoutée.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void onSupprimerUfrClick() {
        Ufr ufr = ufrTable.getSelectionModel().getSelectedItem();
        if (ufr == null) {
            messageLabel.setText("Sélectionne une UFR d'abord.");
            return;
        }
        try {
            ufrDao.delete(ufr.getIdUfr());
            rafraichirTout();
            messageLabel.setText("UFR supprimée.");
        } catch (SQLException e) {
            messageLabel.setText("Impossible de supprimer : des filières y sont probablement rattachées.");
        }
    }

    // --- Filiere ---
    @FXML
    private void onAjouterFiliereClick() {
        String nom = filiereNomField.getText();
        Ufr ufr = filiereUfrComboBox.getValue();
        if (nom.isBlank() || ufr == null) {
            messageLabel.setText("Renseigne un nom et choisis une UFR.");
            return;
        }
        try {
            filiereDao.create(new Filiere(0, nom, ufr.getIdUfr()));
            filiereNomField.clear();
            rafraichirTout();
            messageLabel.setText("Filière ajoutée.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void onSupprimerFiliereClick() {
        Filiere filiere = filiereTable.getSelectionModel().getSelectedItem();
        if (filiere == null) {
            messageLabel.setText("Sélectionne une filière d'abord.");
            return;
        }
        try {
            filiereDao.delete(filiere.getIdFiliere());
            rafraichirTout();
            messageLabel.setText("Filière supprimée.");
        } catch (SQLException e) {
            messageLabel.setText("Impossible de supprimer : des étudiants y sont probablement rattachés.");
        }
    }

    // --- Niveau ---
    @FXML
    private void onAjouterNiveauClick() {
        String libelle = niveauLibelleField.getText();
        if (libelle.isBlank()) {
            messageLabel.setText("Renseigne un libellé.");
            return;
        }
        try {
            niveauDao.create(new Niveau(0, libelle));
            niveauLibelleField.clear();
            rafraichirTout();
            messageLabel.setText("Niveau ajouté.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void onSupprimerNiveauClick() {
        Niveau niveau = niveauTable.getSelectionModel().getSelectedItem();
        if (niveau == null) {
            messageLabel.setText("Sélectionne un niveau d'abord.");
            return;
        }
        try {
            niveauDao.delete(niveau.getIdNiveau());
            rafraichirTout();
            messageLabel.setText("Niveau supprimé.");
        } catch (SQLException e) {
            messageLabel.setText("Impossible de supprimer : des étudiants y sont probablement rattachés.");
        }
    }

    // --- Matiere ---
    @FXML
    private void onAjouterMatiereClick() {
        String nom = matiereNomField.getText();
        if (nom.isBlank()) {
            messageLabel.setText("Renseigne un nom de matière.");
            return;
        }
        try {
            matiereDao.create(new Matiere(0, nom));
            matiereNomField.clear();
            rafraichirTout();
            messageLabel.setText("Matière ajoutée.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void onSupprimerMatiereClick() {
        Matiere matiere = matiereTable.getSelectionModel().getSelectedItem();
        if (matiere == null) {
            messageLabel.setText("Sélectionne une matière d'abord.");
            return;
        }
        try {
            matiereDao.delete(matiere.getIdMatiere());
            rafraichirTout();
            messageLabel.setText("Matière supprimée.");
        } catch (SQLException e) {
            messageLabel.setText("Impossible de supprimer : des demandes ou expertises y sont probablement rattachées.");
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