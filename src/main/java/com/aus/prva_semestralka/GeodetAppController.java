package com.aus.prva_semestralka;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.QuadTree;

public class GeodetAppController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
		QuadTree quadTree = new QuadTree(30, 100, 100);

		GpsPozicia gpsPozicia1Parcela = new GpsPozicia('S', 'V', 24.00, 49.00);
		GpsPozicia gpsPozicia2Parcela = new GpsPozicia('S', 'V', 30.00, 60.00);
		Parcela parcela = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela, gpsPozicia2Parcela));
		quadTree.pridaj(parcela);

		GpsPozicia gpsPozicia1Parcela2 = new GpsPozicia('S', 'V', 4.00, 49.00);
		GpsPozicia gpsPozicia2Parcela2 = new GpsPozicia('S', 'V', 20.00, 65.00);
		Parcela parcela2 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela2, gpsPozicia2Parcela2));
		quadTree.pridaj(parcela2);

		GpsPozicia gpsPozicia1Parcela3 = new GpsPozicia('S', 'V', 27.00, 50.00);
		GpsPozicia gpsPozicia2Parcela3 = new GpsPozicia('S', 'V', 30.00, 59.00);
		Parcela parcela3 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela3, gpsPozicia2Parcela3));
		quadTree.pridaj(parcela3);

		GpsPozicia gpsPozicia1Parcela4 = new GpsPozicia('S', 'V', 40.00, 65.00);
		GpsPozicia gpsPozicia2Parcela4 = new GpsPozicia('S', 'V', 45.00, 67.00);
		Parcela parcela4 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela4, gpsPozicia2Parcela4));
		quadTree.pridaj(parcela4);

		GpsPozicia gpsPozicia1Nehnutelnost = new GpsPozicia('S', 'V', 23.00, 39.00);
		GpsPozicia gpsPozicia2Nehnutelnost = new GpsPozicia('S', 'V', 29.00, 54.00);
		Nehnutelnost nehnutelnost = new Nehnutelnost(123, "dom", List.of(gpsPozicia1Nehnutelnost, gpsPozicia2Nehnutelnost));
		quadTree.pridaj(nehnutelnost);
    }
}