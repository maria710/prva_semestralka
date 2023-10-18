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
         FXMLLoader fxmlLoader = new FXMLLoader(GeodetApp.class.getResource("hello-view.fxml"));
//		GridPane root = new GridPane();
//		root.setAlignment(Pos.CENTER);
//		root.setHgap(10);
//		root.setVgap(10);
//
//		Label welcomeText = new Label("Welcome to JavaFX Application!");
//		welcomeText.setTextFill(Color.GREEN);
//		welcomeText.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
//		root.getChildren().add(welcomeText);

        Scene scene = new Scene(fxmlLoader.load(), 920, 640);
        stage.setTitle("GeodetApp");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}