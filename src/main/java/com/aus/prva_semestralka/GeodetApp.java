package com.aus.prva_semestralka;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GeodetApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(GeodetApp.class.getResource("geodetApp-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 750);
        stage.setTitle("GeodetApp");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}