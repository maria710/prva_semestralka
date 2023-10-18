package com.aus.prva_semestralka;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GeodetAppController {
    @FXML
    private Label pridajParceluLabel;
	@FXML
	TextArea supisneCisloTextField;

    @FXML
    protected void onPridajParceluClick() {
        //welcomeText.setText("Welcome to JavaFX Application!");

		pridajParceluLabel.setText("Zadaj udaje o parcele:");
		var supisneCislo = supisneCisloTextField.getText();

		GeodetAppManazer manazer = new GeodetAppManazer();


    }
}