package com.uadb.mentoruadb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée de l'application Mentor-UADB.
 * Charge l'écran fusionné d'accueil et de connexion au démarrage.
 */
public class Main extends Application {

    /** Taille souhaitée au démarrage. */
    private static final double LARGEUR_IDEALE = 950.0;
    private static final double HAUTEUR_IDEALE = 700.0;
    /** Part maximale de l'écran occupée par la fenêtre au démarrage. */
    private static final double PART_ECRAN = 0.9;
    /** Taille minimale : en dessous, les écrans (sidebar, tableaux, déconnexion) sont coupés. */
    private static final double LARGEUR_MIN = 900.0;
    private static final double HAUTEUR_MIN = 640.0;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/uadb/mentoruadb/fxml/login.fxml"));

        Rectangle2D zone = Screen.getPrimary().getVisualBounds();
        // La fenêtre ne doit jamais dépasser l'écran : au-delà, le bas (bouton
        // de déconnexion) passe sous la barre des tâches et devient invisible.
        double largeurMin = Math.min(LARGEUR_MIN, zone.getWidth() * 0.92);
        double hauteurMin = Math.min(HAUTEUR_MIN, zone.getHeight() * 0.92);
        // Taille initiale toujours >= minimum : évite un saut de taille au démarrage.
        double largeur = Math.max(Math.min(LARGEUR_IDEALE, zone.getWidth() * PART_ECRAN), largeurMin);
        double hauteur = Math.max(Math.min(HAUTEUR_IDEALE, zone.getHeight() * PART_ECRAN), hauteurMin);

        Scene scene = new Scene(root, largeur, hauteur);
        scene.getStylesheets().add(
                getClass().getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm());
        // Design system chargé après style.css : il fait autorité sur les tokens.
        scene.getStylesheets().add(
                getClass().getResource("/com/uadb/mentoruadb/css/theme.css").toExternalForm());

        stage.setTitle("Mentor-UADB");
        stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/uadb/mentoruadb/images/logo-uadb.png")));
        stage.setMinWidth(largeurMin);
        stage.setMinHeight(hauteurMin);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}