package com.aus.prva_semestralka;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class GeodetApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(GeodetApp.class.getResource("geodetApp-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("GeodetApp");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}