package com.uadb.mentoruadb.util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centralise le changement d'écran pour éviter de dupliquer le code de
 * chargement FXML.
 * La fenêtre garde sa taille actuelle d'un écran à l'autre.
 */
public class SceneNavigator {

    /** Taille utilisée seulement si la fenêtre n'est pas encore affichée. */
    private static final double LARGEUR_PAR_DEFAUT = 950.0;
    private static final double HAUTEUR_PAR_DEFAUT = 700.0;

    private SceneNavigator() {
    }

    public static void switchTo(Stage stage, String fxmlPath, String titre) throws IOException {
        Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxmlPath));
        appliquerScene(stage, root, titre);
    }

    /**
     * Comme switchTo, mais renvoie le contrôleur de l'écran chargé pour lui passer
     * des données.
     */
    public static <T> T switchToAndGetController(Stage stage, String fxmlPath, String titre) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        appliquerScene(stage, root, titre);
        return loader.getController();
    }

    private static void appliquerScene(Stage stage, Parent root, String titre) {
        double largeur = stage.isShowing() ? stage.getWidth() : LARGEUR_PAR_DEFAUT;
        double hauteur = stage.isShowing() ? stage.getHeight() : HAUTEUR_PAR_DEFAUT;

        Scene scene = new Scene(root, largeur, hauteur);
        scene.getStylesheets().add(
                SceneNavigator.class.getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm());
        // Design system chargé après style.css : il fait autorité sur les tokens.
        scene.getStylesheets().add(
                SceneNavigator.class.getResource("/com/uadb/mentoruadb/css/theme.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(titre);
        limiterALaFenetreAEcran(stage);
    }

    /**
     * Borne la fenêtre aux dimensions utiles de l'écran où elle se trouve :
     * au-delà, le bas de la fenêtre (boutons, déconnexion) passe sous la barre
     * des tâches et devient invisible. Utile si l'utilisateur place la fenêtre
     * sur un second écran plus petit.
     */
    private static void limiterALaFenetreAEcran(Stage stage) {
        double largeur = stage.getWidth();
        double hauteur = stage.getHeight();
        Rectangle2D zone = Screen.getScreensForRectangle(stage.getX(), stage.getY(), largeur, hauteur)
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary())
                .getVisualBounds();

        if (largeur > zone.getWidth()) {
            stage.setWidth(zone.getWidth());
        }
        if (hauteur > zone.getHeight()) {
            stage.setHeight(zone.getHeight());
        }
        if (stage.getMinWidth() > zone.getWidth()) {
            stage.setMinWidth(zone.getWidth());
        }
        if (stage.getMinHeight() > zone.getHeight()) {
            stage.setMinHeight(zone.getHeight());
        }
    }
}