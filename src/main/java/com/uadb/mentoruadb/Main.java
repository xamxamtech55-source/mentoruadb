package com.uadb.mentoruadb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée de l'application MentorUADB.
 * Charge l'écran de connexion au démarrage.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/uadb/mentoruadb/fxml/login.fxml")
        );

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/com/uadb/mentoruadb/css/style.css").toExternalForm()
        );

        stage.setTitle("MentorUADB");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
