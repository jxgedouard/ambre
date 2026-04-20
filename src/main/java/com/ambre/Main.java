package com.ambre;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/ambre/fxml/login.fxml")
        );

        Scene scene = new Scene(loader.load(), 440, 520);
        scene.getStylesheets().add(
            getClass().getResource("/com/ambre/styles/ambre.css").toExternalForm()
        );

        primaryStage.setTitle("Ambre");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
