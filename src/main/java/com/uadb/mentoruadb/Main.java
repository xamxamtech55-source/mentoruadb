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
 * Charge l'écran d'accueil au démarrage, qui mène ensuite à la connexion.
 */
public class Main extends Application {

    /** Taille souhaitée au démarrage. */
    private static final double LARGEUR_IDEALE = 950.0;
    private static final double HAUTEUR_IDEALE = 700.0;
    /** Part maximale de l'écran occupée par la fenêtre au démarrage. */
    private static final double PART_ECRAN = 0.9;
    /** Taille minimale : en dessous, les écrans ne sont plus utilisables. */
    private static final double LARGEUR_MIN = 360.0;
    private static final double HAUTEUR_MIN = 520.0;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/uadb/mentoruadb/fxml/bienvenue.fxml"));

        Rectangle2D zone = Screen.getPrimary().getVisualBounds();
        double largeur = Math.min(LARGEUR_IDEALE, zone.getWidth() * PART_ECRAN);
        double hauteur = Math.min(HAUTEUR_IDEALE, zone.getHeight() * PART_ECRAN);

        Scene scene = new Scene(root, largeur, hauteur);
        scene.getStylesheets().add(
                getClass().getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm());

        stage.setTitle("Mentor-UADB");
        stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/uadb/mentoruadb/images/logo-uadb.png")));
        stage.setMinWidth(LARGEUR_MIN);
        stage.setMinHeight(HAUTEUR_MIN);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}