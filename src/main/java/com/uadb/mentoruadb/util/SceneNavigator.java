package com.uadb.mentoruadb.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Centralise le changement d'écran pour éviter de dupliquer le code de chargement FXML. */
public class SceneNavigator {

    private static final double LARGEUR_FENETRE = 950.0;
    private static final double HAUTEUR_FENETRE = 700.0;

    private SceneNavigator() {}

    public static void switchTo(Stage stage, String fxmlPath, String titre) throws IOException {
        Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxmlPath));
        appliquerScene(stage, root, titre);
    }

    /** Comme switchTo, mais renvoie le contrôleur de l'écran chargé pour lui passer des données. */
    public static <T> T switchToAndGetController(Stage stage, String fxmlPath, String titre) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        appliquerScene(stage, root, titre);
        return loader.getController();
    }

    private static void appliquerScene(Stage stage, Parent root, String titre) {
        Scene scene = new Scene(root, LARGEUR_FENETRE, HAUTEUR_FENETRE);
        scene.getStylesheets().add(
                SceneNavigator.class.getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setTitle(titre);
    }
}