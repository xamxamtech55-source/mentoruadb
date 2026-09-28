package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.FiliereDao;
import com.uadb.mentoruadb.dao.FiliereMatiereDao;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Contrôleur lié à fxml/gestion-donnees.fxml : arbre UFR → Filières → Matières + catalogue + niveaux. */
public class GestionDonneesController {

    // --- Onglet Organisation ---
    @FXML private TreeView<Object> organisationTree;
    @FXML private TextField ufrNomField;
    @FXML private TextField filiereNomField;
    @FXML private ListView<Niveau> niveauxListView;
    @FXML private ComboBox<Matiere> matiereRattacherComboBox;

    // --- Onglet Catalogue des matières ---
    @FXML private TableView<Matiere> matiereTable;
    @FXML private TableColumn<Matiere, String> colMatiereNom;
    @FXML private TextField matiereNomField;

    // --- Onglet Niveaux ---
    @FXML private TableView<Niveau> niveauTable;
    @FXML private TableColumn<Niveau, String> colNiveauLibelle;
    @FXML private TextField niveauLibelleField;

    @FXML private Label messageLabel;
    @FXML private Button retourButton;

    private final UfrDao ufrDao = new UfrDao();
    private final FiliereDao filiereDao = new FiliereDao();
    private final NiveauDao niveauDao = new NiveauDao();
    private final MatiereDao matiereDao = new MatiereDao();
    private final FiliereMatiereDao filiereMatiereDao = new FiliereMatiereDao();

    @FXML
    public void initialize() {
        colMatiereNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveauLibelle.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        niveauxListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Affichage de l'arbre : UFR en gras, filières normales, matières avec une puce
        organisationTree.setCellFactory(tree -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else if (item instanceof Ufr) {
                    setText(((Ufr) item).getNom());
                    setStyle("-fx-font-weight: bold;");
                } else if (item instanceof Filiere) {
                    setText(((Filiere) item).getNom());
                    setStyle("");
                } else if (item instanceof Matiere) {
                    setText("• " + ((Matiere) item).getNom());
                    setStyle("-fx-text-fill: #555555;");
                } else {
                    setText(item.toString());
                    setStyle("");
                }
            }
        });

        rafraichirTout();
    }

    // ---------------------------------------------------------------
    // Chargement / rafraîchissement
    // ---------------------------------------------------------------

    private void rafraichirTout() {
        // On mémorise les nœuds ouverts pour ne pas tout refermer après chaque action
        Set<String> ouverts = new HashSet<>();
        if (organisationTree.getRoot() != null) {
            collecterOuverts(organisationTree.getRoot(), ouverts);
        }

        try {
            TreeItem<Object> racine = new TreeItem<>("Organisation");
            racine.setExpanded(true);

            for (Ufr ufr : ufrDao.findAll()) {
                TreeItem<Object> ufrItem = new TreeItem<>(ufr);
                ufrItem.setExpanded(ouverts.contains(cle(ufr)));

                for (Filiere filiere : filiereDao.findByUfr(ufr.getIdUfr())) {
                    TreeItem<Object> filiereItem = new TreeItem<>(filiere);
                    filiereItem.setExpanded(ouverts.contains(cle(filiere)));

                    for (Matiere matiere : filiereDao.findMatieresByFiliere(filiere.getIdFiliere())) {
                        filiereItem.getChildren().add(new TreeItem<>(matiere));
                    }
                    ufrItem.getChildren().add(filiereItem);
                }
                racine.getChildren().add(ufrItem);
            }

            organisationTree.setRoot(racine);
            organisationTree.setShowRoot(false);

            niveauxListView.setItems(FXCollections.observableArrayList(niveauDao.findAll()));
            matiereRattacherComboBox.setItems(FXCollections.observableArrayList(matiereDao.findAll()));
            matiereTable.setItems(FXCollections.observableArrayList(matiereDao.findAll()));
            niveauTable.setItems(FXCollections.observableArrayList(niveauDao.findAll()));
        } catch (SQLException e) {
            messageLabel.setText("Erreur de chargement des données.");
        }
    }

    /** Clé stable d'un nœud (UFR ou filière) pour retrouver son état ouvert/fermé après rechargement. */
    private String cle(Object valeur) {
        if (valeur instanceof Ufr) return "U" + ((Ufr) valeur).getIdUfr();
        if (valeur instanceof Filiere) return "F" + ((Filiere) valeur).getIdFiliere();
        return null;
    }

    private void collecterOuverts(TreeItem<Object> item, Set<String> ouverts) {
        String cle = cle(item.getValue());
        if (cle != null && item.isExpanded()) {
            ouverts.add(cle);
        }
        for (TreeItem<Object> enfant : item.getChildren()) {
            collecterOuverts(enfant, ouverts);
        }
    }

    // ---------------------------------------------------------------
    // Lecture de la sélection dans l'arbre
    // ---------------------------------------------------------------

    /** UFR sélectionnée, ou UFR parente si l'on a cliqué sur une filière ou une matière. */
    private Ufr ufrSelectionnee() {
        TreeItem<Object> item = organisationTree.getSelectionModel().getSelectedItem();
        while (item != null) {
            if (item.getValue() instanceof Ufr) {
                return (Ufr) item.getValue();
            }
            item = item.getParent();
        }
        return null;
    }

    /** Filière sélectionnée, ou filière parente si l'on a cliqué sur une matière. */
    private Filiere filiereSelectionnee() {
        TreeItem<Object> item = organisationTree.getSelectionModel().getSelectedItem();
        while (item != null) {
            if (item.getValue() instanceof Filiere) {
                return (Filiere) item.getValue();
            }
            item = item.getParent();
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Onglet Organisation : UFR
    // ---------------------------------------------------------------

    @FXML
    private void onAjouterUfrClick() {
        String nom = ufrNomField.getText();
        if (nom.isBlank()) {
            messageLabel.setText("Renseigne un nom d'UFR.");
            return;
        }
        try {
            ufrDao.create(new Ufr(0, nom.trim()));
            ufrNomField.clear();
            rafraichirTout();
            messageLabel.setText("UFR ajoutée.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout (ce nom d'UFR existe peut-être déjà).");
        }
    }

    // ---------------------------------------------------------------
    // Onglet Organisation : Filières
    // ---------------------------------------------------------------

    @FXML
    private void onAjouterFiliereClick() {
        Ufr ufr = ufrSelectionnee();
        String nom = filiereNomField.getText();
        List<Niveau> niveaux = new ArrayList<>(niveauxListView.getSelectionModel().getSelectedItems());

        if (ufr == null) {
            messageLabel.setText("Sélectionne d'abord une UFR (ou l'une de ses filières) dans l'arbre.");
            return;
        }
        if (nom.isBlank()) {
            messageLabel.setText("Renseigne un nom de filière.");
            return;
        }
        if (niveaux.isEmpty()) {
            messageLabel.setText("Choisis au moins un niveau : sans niveau, personne ne pourrait s'inscrire dans cette filière.");
            return;
        }

        try {
            Filiere filiere = filiereDao.create(new Filiere(0, nom.trim(), ufr.getIdUfr()));
            for (Niveau niveau : niveaux) {
                filiereDao.associerNiveau(filiere.getIdFiliere(), niveau.getIdNiveau());
            }
            filiereNomField.clear();
            niveauxListView.getSelectionModel().clearSelection();
            rafraichirTout();
            messageLabel.setText("Filière « " + filiere.getNom() + " » ajoutée à " + ufr.getNom() + ".");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout (cette filière existe peut-être déjà dans cette UFR).");
        }
    }

    // ---------------------------------------------------------------
    // Onglet Organisation : rattachement matière <-> filière
    // ---------------------------------------------------------------

    @FXML
    private void onRattacherMatiereClick() {
        Filiere filiere = filiereSelectionnee();
        Matiere matiere = matiereRattacherComboBox.getValue();

        if (filiere == null) {
            messageLabel.setText("Sélectionne d'abord une filière dans l'arbre.");
            return;
        }
        if (matiere == null) {
            messageLabel.setText("Choisis une matière du catalogue à rattacher.");
            return;
        }

        try {
            filiereMatiereDao.ajouter(filiere.getIdFiliere(), matiere.getIdMatiere());
            rafraichirTout();
            messageLabel.setText("« " + matiere.getNom() + " » rattachée à " + filiere.getNom() + ".");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du rattachement.");
        }
    }

    @FXML
    private void onDetacherMatiereClick() {
        TreeItem<Object> item = organisationTree.getSelectionModel().getSelectedItem();
        if (item == null || !(item.getValue() instanceof Matiere)) {
            messageLabel.setText("Sélectionne une matière (sous une filière) dans l'arbre.");
            return;
        }

        Matiere matiere = (Matiere) item.getValue();
        Filiere filiere = (Filiere) item.getParent().getValue();

        try {
            filiereMatiereDao.retirer(filiere.getIdFiliere(), matiere.getIdMatiere());
            rafraichirTout();
            messageLabel.setText("« " + matiere.getNom() + " » détachée de " + filiere.getNom() + ".");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors du détachement.");
        }
    }

    // ---------------------------------------------------------------
    // Onglet Organisation : suppression UFR / filière
    // ---------------------------------------------------------------

    @FXML
    private void onSupprimerSelectionClick() {
        TreeItem<Object> item = organisationTree.getSelectionModel().getSelectedItem();
        if (item == null) {
            messageLabel.setText("Sélectionne une UFR ou une filière dans l'arbre.");
            return;
        }

        Object valeur = item.getValue();
        try {
            if (valeur instanceof Ufr) {
                ufrDao.delete(((Ufr) valeur).getIdUfr());
                messageLabel.setText("UFR supprimée.");
            } else if (valeur instanceof Filiere) {
                filiereDao.delete(((Filiere) valeur).getIdFiliere());
                messageLabel.setText("Filière supprimée.");
            } else {
                messageLabel.setText("Pour une matière, utilise « Détacher la matière sélectionnée ».");
                return;
            }
            rafraichirTout();
        } catch (SQLException e) {
            messageLabel.setText("Suppression impossible : des filières ou des étudiants y sont encore rattachés.");
        }
    }

    // ---------------------------------------------------------------
    // Onglet Catalogue des matières
    // ---------------------------------------------------------------

    @FXML
    private void onAjouterMatiereClick() {
        String nom = matiereNomField.getText();
        if (nom.isBlank()) {
            messageLabel.setText("Renseigne un nom de matière.");
            return;
        }
        try {
            matiereDao.create(new Matiere(0, nom.trim()));
            matiereNomField.clear();
            rafraichirTout();
            messageLabel.setText("Matière ajoutée au catalogue — rattache-la à une filière pour qu'elle soit visible.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout (cette matière existe peut-être déjà).");
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
            messageLabel.setText("Impossible de supprimer : des demandes de mentorat y sont rattachées.");
        }
    }

    // ---------------------------------------------------------------
    // Onglet Niveaux
    // ---------------------------------------------------------------

    @FXML
    private void onAjouterNiveauClick() {
        String libelle = niveauLibelleField.getText();
        if (libelle.isBlank()) {
            messageLabel.setText("Renseigne un libellé.");
            return;
        }
        try {
            niveauDao.create(new Niveau(0, libelle.trim()));
            niveauLibelleField.clear();
            rafraichirTout();
            messageLabel.setText("Niveau ajouté.");
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'ajout (ce niveau existe peut-être déjà).");
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
            messageLabel.setText("Impossible de supprimer : des étudiants y sont rattachés.");
        }
    }

    // ---------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------

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