package com.uadb.mentoruadb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée de l'application Mentor-UADB.
 * Charge l'écran d'accueil au démarrage, qui mène ensuite à la connexion.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/uadb/mentoruadb/fxml/bienvenue.fxml")
        );

        Scene scene = new Scene(root, 950, 700);
        scene.getStylesheets().add(
                getClass().getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm()
        );

        stage.setTitle("Mentor-UADB");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}