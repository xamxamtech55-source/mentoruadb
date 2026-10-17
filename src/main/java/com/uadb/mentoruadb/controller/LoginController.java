package com.uadb.mentoruadb.controller;

import com.uadb.mentoruadb.dao.EtudiantDao;
import com.uadb.mentoruadb.model.Etudiant;
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.AuthService;
import com.uadb.mentoruadb.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Contrôleur lié à fxml/login.fxml.
 * Redirige chaque rôle vers son propre espace : ADMIN vers l'administration,
 * ETUDIANT et MENTOR vers l'espace étudiant.
 */
public class LoginController {

    /** Stockage local : seul l'email est mémorisé, jamais le mot de passe. */
    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginController.class);
    private static final String CLE_EMAIL_MEMORISE = "email_memorise";

    /** En dessous de cette largeur, le panneau de marque est masqué. */
    private static final double SEUIL_COMPACT = 760.0;

    private static final String TITRE_DEFAUT = "Connexion à votre compte";
    private static final String SOUS_TITRE_DEFAUT = "Accédez à votre espace MentorUADB et poursuivez votre parcours.";
    private static final String BOUTON_DEFAUT = "Se connecter  →";
    private static final String BOUTON_ADMIN_DEFAUT = "✔  Accès administrateur";

    @FXML
    private HBox racine;
    @FXML
    private StackPane panneauGauche;
    @FXML
    private ImageView imageGauche;
    @FXML
    private Label titreLabel;
    @FXML
    private Label sousTitreLabel;
    @FXML
    private TextField emailField;
    @FXML
    private CheckBox souvenirCheckBox;
    @FXML
    private PasswordField motDePasseField;
    @FXML
    private TextField motDePasseVisibleField;
    @FXML
    private Button toggleMotDePasseButton;
    @FXML
    private Button connexionButton;
    @FXML
    private Button accesAdminButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Button creerCompteButton;
    @FXML
    private Button motDePasseOublieButton;
    @FXML
    private Button retourAccueilButton;

    private final AuthService authService = new AuthService();
    private final EtudiantDao etudiantDao = new EtudiantDao();

    private boolean motDePasseVisible = false;
    /** true = mode « accès administrateur » : seul un compte ADMIN peut entrer. */
    private boolean modeAdmin = false;

    @FXML
    public void initialize() {
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());

        emailField.setOnAction(e -> onConnexionClick());
        motDePasseField.setOnAction(e -> onConnexionClick());

        // L'image suit la largeur du panneau gauche.
        imageGauche.fitWidthProperty().bind(panneauGauche.widthProperty());
        racine.widthProperty().addListener((obs, ancienne, nouvelle) -> adapterMiseEnPage(nouvelle.doubleValue()));
        adapterMiseEnPage(racine.getWidth());

        String emailMemorise = PREFS.get(CLE_EMAIL_MEMORISE, "");
        if (!emailMemorise.isBlank()) {
            emailField.setText(emailMemorise);
            souvenirCheckBox.setSelected(true);
            Platform.runLater(motDePasseField::requestFocus);
        }
    }

    /**
     * Masque le panneau gauche en petite fenêtre et lui donne une largeur
     * proportionnelle sinon.
     */
    private void adapterMiseEnPage(double largeur) {
        boolean compact = largeur < SEUIL_COMPACT;
        panneauGauche.setVisible(!compact);
        panneauGauche.setManaged(!compact);

        double largeurGauche = Math.min(460.0, Math.max(300.0, largeur * 0.42));
        panneauGauche.setMinWidth(largeurGauche);
        panneauGauche.setPrefWidth(largeurGauche);
        panneauGauche.setMaxWidth(largeurGauche);
    }

    @FXML
    private void onToggleMotDePasseClick() {
        motDePasseVisible = !motDePasseVisible;
        motDePasseField.setVisible(!motDePasseVisible);
        motDePasseField.setManaged(!motDePasseVisible);
        motDePasseVisibleField.setVisible(motDePasseVisible);
        motDePasseVisibleField.setManaged(motDePasseVisible);
        toggleMotDePasseButton.setText(motDePasseVisible ? "🙈" : "👁");
    }

    /** Bascule entre la connexion étudiant et la connexion administrateur. */
    @FXML
    private void onAccesAdminClick() {
        modeAdmin = !modeAdmin;
        messageLabel.setText("");
        if (modeAdmin) {
            titreLabel.setText("Accès administrateur");
            sousTitreLabel.setText("Espace réservé aux comptes administrateurs de l'UADB.");
            connexionButton.setText("Se connecter en administrateur  →");
            accesAdminButton.setText("← Connexion étudiant");
        } else {
            titreLabel.setText(TITRE_DEFAUT);
            sousTitreLabel.setText(SOUS_TITRE_DEFAUT);
            connexionButton.setText(BOUTON_DEFAUT);
            accesAdminButton.setText(BOUTON_ADMIN_DEFAUT);
        }
    }

    @FXML
    private void onConnexionClick() {
        String email = emailField.getText();
        String motDePasse = motDePasseField.getText();

        if (email.isBlank() || motDePasse.isBlank()) {
            messageLabel.setText("Renseigne l'email et le mot de passe.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.connecter(email, motDePasse);
            if (utilisateur == null) {
                messageLabel.setText("Email ou mot de passe incorrect.");
                return;
            }

            // En mode administrateur, seul le rôle ADMIN est accepté.
            if (modeAdmin && !"ADMIN".equals(utilisateur.getRole())) {
                messageLabel.setText("Ce compte n'a pas accès à l'espace administrateur.");
                return;
            }

            memoriserEmail(email);

            switch (utilisateur.getRole()) {
                case "ETUDIANT", "MENTOR" -> ouvrirEspaceEtudiant(utilisateur);
                case "ADMIN" -> ouvrirEspaceAdmin(utilisateur);
                default -> messageLabel.setText("Rôle non reconnu : " + utilisateur.getRole());
            }

        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données.");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir le tableau de bord.");
        }
    }

    /** Espace étudiant : valable pour l'étudiant simple comme pour le mentor. */
    private void ouvrirEspaceEtudiant(Utilisateur utilisateur) throws SQLException, IOException {
        Optional<Etudiant> etudiantOpt = etudiantDao.findByUtilisateur(utilisateur.getIdUtilisateur());
        if (etudiantOpt.isEmpty()) {
            messageLabel.setText("Profil étudiant introuvable pour ce compte.");
            return;
        }

        Stage stage = (Stage) racine.getScene().getWindow();
        DashboardEtudiantController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/dashboard-etudiant.fxml", "Mentor-UADB - Tableau de bord");
        controller.chargerDonnees(etudiantOpt.get(), utilisateur.getPrenom());
    }

    /** Espace administrateur, distinct de l'espace étudiant. */
    private void ouvrirEspaceAdmin(Utilisateur utilisateur) throws IOException {
        Stage stage = (Stage) racine.getScene().getWindow();
        DashboardAdminController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/dashboard-admin.fxml", "Mentor-UADB - Administration");
        controller.chargerDonnees(utilisateur.getPrenom());
    }

    /**
     * Mémorise l'email si la case est cochée, sinon oublie celui qui avait été
     * mémorisé.
     */
    private void memoriserEmail(String email) {
        if (souvenirCheckBox.isSelected()) {
            PREFS.put(CLE_EMAIL_MEMORISE, email.trim());
        } else {
            PREFS.remove(CLE_EMAIL_MEMORISE);
        }
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            // Non bloquant : au pire l'email ne sera pas pré-rempli la prochaine fois.
        }
    }

    @FXML
    private void onCreerCompteClick() {
        try {
            Stage stage = (Stage) creerCompteButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/inscription.fxml", "Mentor-UADB - Inscription");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran d'inscription.");
        }
    }

    @FXML
    private void onMotDePasseOublieClick() {
        try {
            Stage stage = (Stage) motDePasseOublieButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/mot-de-passe-oublie.fxml",
                    "Mentor-UADB - Mot de passe oublié");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }

    @FXML
    private void onRetourAccueilClick() {
        try {
            Stage stage = (Stage) retourAccueilButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/bienvenue.fxml", "Mentor-UADB");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'écran d'accueil.");
        }
    }
}