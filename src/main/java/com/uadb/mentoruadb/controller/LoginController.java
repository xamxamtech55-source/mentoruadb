package com.uadb.mentoruadb.controller;

// ==================== DÉBUT IMPORTS ====================
import com.uadb.mentoruadb.model.Utilisateur;
import com.uadb.mentoruadb.service.AuthService;
import com.uadb.mentoruadb.util.ChampTelephone;
import com.uadb.mentoruadb.util.MessageLabel;
import com.uadb.mentoruadb.util.SceneNavigator;
import com.uadb.mentoruadb.util.Validateur;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
// ==================== FIN IMPORTS ====================

// ==================== DÉBUT CLASSE ====================
/**
 * Contrôleur lié à fxml/login.fxml : écran fusionné d'accueil et de connexion.
 * Fond panoramique (photo + voile bleu nuit), bloc institutionnel à gauche,
 * carte de connexion flottante à droite. La page s'adapte à la taille de la
 * fenêtre (formulaire resserré sur les écrans peu hauts) pour rester lisible
 * sans barre de défilement. Formulaire des étudiants et des mentors : le
 * numéro de carte y est obligatoire. Les administrateurs passent par le bouton
 * « Accès administrateur » qui ouvre l'écran de connexion dédié.
 */
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    /** Stockage local : seul l'email est mémorisé, jamais le mot de passe. */
    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginController.class);
    private static final String CLE_EMAIL_MEMORISE = "email_memorise";

    /** En dessous de cette largeur, le bloc institutionnel est masqué. */
    private static final double SEUIL_COMPACT = 820.0;
    /** En dessous de cette hauteur, le formulaire passe en mode resserré. */
    private static final double SEUIL_HAUTEUR_COMPACT = 700.0;
    /** En dessous de cette hauteur, on masque les éléments décoratifs. */
    private static final double SEUIL_HAUTEUR_MINIMAL = 540.0;
    /** Largeur maximale réservée au bloc institutionnel (part de la zone utile). */
    private static final double PART_BLOC_INSTITUTIONNEL = 0.45;

    @FXML
    private ScrollPane scrollRacine;
    @FXML
    private StackPane racine;
    @FXML
    private HBox conteneurHero;
    @FXML
    private VBox blocInstitutionnel;
    @FXML
    private Label citationLabel;
    @FXML
    private Label deviseLabel;
    @FXML
    private Label piluleLabel;
    @FXML
    private VBox zoneCarte;
    @FXML
    private VBox carteConnexion;
    @FXML
    private Region espaceSousTitre;
    @FXML
    private Region espaceAvantMotDePasse;
    @FXML
    private Region espaceApresMotDePasse;
    @FXML
    private Region espaceAvantConnexion;
    @FXML
    private Region espaceAvantPied;
    @FXML
    private Label titreLabel;
    @FXML
    private Label sousTitreLabel;
    @FXML
    private Label piedLabel;
    @FXML
    private TextField emailField;
    @FXML
    private TextField numeroCarteField;
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
    private Label messageLabel;
    @FXML
    private Button motDePasseOublieButton;
    @FXML
    private Button premiereConnexionButton;
    @FXML
    private Button accesAdminButton;

    private final AuthService authService = new AuthService();

    private boolean motDePasseVisible = false;
    private boolean hauteurMinimale = false;

    @FXML
    public void initialize() {
        MessageLabel.rendreDynamique(messageLabel);
        motDePasseVisibleField.textProperty().bindBidirectional(motDePasseField.textProperty());

        emailField.setOnAction(e -> onConnexionClick());
        motDePasseField.setOnAction(e -> onConnexionClick());
        ChampTelephone.chiffres(numeroCarteField, 7);

        // Le contenu occupe au moins la hauteur visible (pas de bande vide en bas).
        racine.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> scrollRacine.getViewportBounds().getHeight(), scrollRacine.viewportBoundsProperty()));

        racine.widthProperty().addListener((obs, ancienne, nouvelle) -> adapterMiseEnPage());
        adapterMiseEnPage();

        // Pas de barre de défilement : le formulaire se resserre quand la hauteur baisse.
        scrollRacine.viewportBoundsProperty().addListener(
                (obs, ancienne, nouvelle) -> adapterHauteur(nouvelle.getHeight()));
        adapterHauteur(scrollRacine.getViewportBounds().getHeight());

        apparitionCarte();

        String emailMemorise = PREFS.get(CLE_EMAIL_MEMORISE, "");
        if (!emailMemorise.isBlank()) {
            emailField.setText(emailMemorise);
            souvenirCheckBox.setSelected(true);
            Platform.runLater(motDePasseField::requestFocus);
        }
    }

    /** Pré-remplit l'email et affiche un message (retour d'un autre écran). */
    public void prefillerEmailEtMessage(String email, String message) {
        if (email != null && !email.isBlank()) {
            emailField.setText(email);
        }
        if (message != null && !message.isBlank()) {
            messageLabel.setText(message);
        }
        Platform.runLater(motDePasseField::requestFocus);
    }

    /**
     * Répartit la largeur entre le bloc institutionnel et la carte : le bloc
     * garde une part limitée pour que le formulaire conserve sa largeur
     * confortable (évite les retours à la ligne qui allongent la page).
     * Masque le bloc institutionnel en fenêtre étroite.
     */
    private void adapterMiseEnPage() {
        double largeur = racine.getWidth();
        boolean masquerBloc = largeur < SEUIL_COMPACT || hauteurMinimale;
        blocInstitutionnel.setVisible(!masquerBloc);
        blocInstitutionnel.setManaged(!masquerBloc);

        if (!masquerBloc) {
            double marges = conteneurHero.getPadding().getLeft() + conteneurHero.getPadding().getRight();
            double largeurUtile = Math.max(0.0, largeur - marges);
            double largeurBloc = Math.min(520.0, largeurUtile * PART_BLOC_INSTITUTIONNEL);
            blocInstitutionnel.setMinWidth(largeurBloc);
            blocInstitutionnel.setPrefWidth(largeurBloc);
            blocInstitutionnel.setMaxWidth(largeurBloc);
        }
        zoneCarte.setMinWidth(0.0);
    }

    /**
     * Resserre le formulaire sur les écrans peu hauts pour que la page tienne
     * sans barre de défilement (le ScrollPane ne reste qu'en secours).
     */
    private void adapterHauteur(double hauteur) {
        if (hauteur <= 0) {
            return;
        }
        boolean compact = hauteur < SEUIL_HAUTEUR_COMPACT;
        hauteurMinimale = hauteur < SEUIL_HAUTEUR_MINIMAL;

        carteConnexion.setSpacing(hauteurMinimale ? 3.0 : (compact ? 4.0 : 6.0));
        if (compact) {
            if (!carteConnexion.getStyleClass().contains("acc-carte-compacte")) {
                carteConnexion.getStyleClass().add("acc-carte-compacte");
            }
        } else {
            carteConnexion.getStyleClass().remove("acc-carte-compacte");
        }
        double margeHorizontale = compact ? 24.0 : 56.0;
        conteneurHero.setPadding(new Insets(compact ? 12.0 : 24.0, margeHorizontale,
                compact ? 12.0 : 24.0, margeHorizontale));
        adapterMiseEnPage();

        sousTitreLabel.setVisible(!hauteurMinimale);
        sousTitreLabel.setManaged(!hauteurMinimale);
        piedLabel.setVisible(!hauteurMinimale);
        piedLabel.setManaged(!hauteurMinimale);

        boolean decorInstitutionnel = !hauteurMinimale && hauteur >= 700.0;
        citationLabel.setVisible(decorInstitutionnel);
        citationLabel.setManaged(decorInstitutionnel);
        deviseLabel.setVisible(decorInstitutionnel);
        deviseLabel.setManaged(decorInstitutionnel);
        piluleLabel.setVisible(decorInstitutionnel);
        piluleLabel.setManaged(decorInstitutionnel);

        for (Region espace : new Region[]{espaceSousTitre, espaceAvantMotDePasse,
                espaceApresMotDePasse, espaceAvantConnexion, espaceAvantPied}) {
            espace.setVisible(!compact);
            espace.setManaged(!compact);
        }
    }

    /** Apparition discrète de la carte (une seule fois, sans boucle). */
    private void apparitionCarte() {
        carteConnexion.setOpacity(0.0);
        carteConnexion.setTranslateY(10.0);

        FadeTransition fondu = new FadeTransition(Duration.millis(260), carteConnexion);
        fondu.setFromValue(0.0);
        fondu.setToValue(1.0);
        fondu.setDelay(Duration.millis(40));

        TranslateTransition levee = new TranslateTransition(Duration.millis(260), carteConnexion);
        levee.setFromY(10.0);
        levee.setToY(0.0);
        levee.setDelay(Duration.millis(40));

        fondu.play();
        levee.play();
    }

    @FXML
    private void onToggleMotDePasseClick() {
        motDePasseVisible = !motDePasseVisible;
        motDePasseField.setVisible(!motDePasseVisible);
        motDePasseField.setManaged(!motDePasseVisible);
        motDePasseVisibleField.setVisible(motDePasseVisible);
        motDePasseVisibleField.setManaged(motDePasseVisible);
        toggleMotDePasseButton.setText(motDePasseVisible ? "Masquer" : "Afficher");
    }

    @FXML
    private void onConnexionClick() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String carte = numeroCarteField.getText() == null ? "" : numeroCarteField.getText().trim();
        String motDePasse = motDePasseField.getText() == null ? "" : motDePasseField.getText();

        if (email.isBlank() || motDePasse.isBlank()) {
            messageLabel.setText("Renseigne l'email et le mot de passe.");
            return;
        }
        if (!Validateur.emailValide(email)) {
            messageLabel.setText("L'adresse email est invalide.");
            return;
        }
        if (!carte.isEmpty() && !Validateur.numeroCarteValide(carte)) {
            messageLabel.setText("Le numéro de carte doit contenir exactement 7 chiffres.");
            return;
        }

        try {
            Utilisateur utilisateur = authService.connecter(email, carte, motDePasse);
            if (utilisateur == null) {
                messageLabel.setText("Email, numéro de carte ou mot de passe incorrect.");
                return;
            }

            // Séparation des espaces : un administrateur ne se connecte pas ici.
            if ("ADMIN".equals(utilisateur.getRole())) {
                messageLabel.setText("Compte administrateur : utilisez le bouton « Accès administrateur ».");
                return;
            }

            memoriserEmail(email);

            if (utilisateur.isMustChangePassword()) {
                ouvrirChangementObligatoire(utilisateur);
                return;
            }

            SessionNavigation.ouvrirEspace((Stage) racine.getScene().getWindow(), utilisateur);

        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données.");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir le tableau de bord.");
        } catch (IllegalStateException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void ouvrirChangementObligatoire(Utilisateur utilisateur) throws IOException {
        Stage stage = (Stage) racine.getScene().getWindow();
        ChangementMotDePasseObligatoireController controller = SceneNavigator.switchToAndGetController(
                stage, "/com/uadb/mentoruadb/fxml/changement-mot-de-passe-obligatoire.fxml",
                "Mentor-UADB - Nouveau mot de passe");
        controller.initialiser(utilisateur);
    }

    /** Ouvre l'écran de connexion réservé aux administrateurs. */
    @FXML
    private void onAccesAdminClick() {
        try {
            Stage stage = (Stage) accesAdminButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/login-admin.fxml",
                    "Mentor-UADB - Accès administrateur");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir l'accès administrateur.");
        }
    }

    @FXML
    private void onPremiereConnexionClick() {
        try {
            if (premiereConnexionButton == null || premiereConnexionButton.getScene() == null) {
                messageLabel.setText("Impossible d'ouvrir la première connexion.");
                return;
            }
            Stage stage = (Stage) premiereConnexionButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/activation-demande.fxml",
                    "Mentor-UADB - Première connexion");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Echec d'ouverture de l'ecran activation-demande", e);
            messageLabel.setText("Impossible d'ouvrir la première connexion. Vérifie les ressources (FXML/CSS) ou les logs.");
        }
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
    private void onMotDePasseOublieClick() {
        try {
            Stage stage = (Stage) motDePasseOublieButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/com/uadb/mentoruadb/fxml/mot-de-passe-oublie.fxml",
                    "Mentor-UADB - Mot de passe oublié");
        } catch (IOException e) {
            messageLabel.setText("Impossible d'ouvrir cet écran.");
        }
    }
}
// ==================== FIN CLASSE ====================
