package com.aus.prva_semestralka;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;

public class GeodetAppController implements Initializable {

	private GeodetAppManazer manazer;

	@FXML
	private ChoiceBox<String> actionChoiceBox;

	@FXML
	private ChoiceBox<String> pozemokChoiceBox;

	@FXML
	private TextField supisneCisloText;

	@FXML
	private TextField popisText;

	@FXML
	private TextField lavaDolnaX;

	@FXML
	private TextField lavaDolnaY;

	@FXML
	private TextField pravaHornaX;

	@FXML
	private TextField pravaHornaY;

	@FXML
	private TextField orientaciaSirkaDolna;

	@FXML
	private TextField orientaciaVyskaDolna;

	@FXML
	private TextField orientaciaSirkaHorna;

	@FXML
	private TextField orientaciaVyskaHorna;

	@FXML
	private Button spustiButton;

	@FXML
	private Button vypisatNehnutelnostiButton;

	@FXML
	private Button vypisParcelyButton;

	@FXML
	private ListView<String> parcelyListView;

	@FXML
	private ListView<String> nehnutelnostiListView;

	@FXML
	private Label resultLabel;

	private final List<String> akcie = List.of("Pridať", "Vymazať", "Upraviť", "Nájsť");
	private final List<String> pozemky = List.of("Nehnuteľnosť", "Parcela");

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		manazer = new GeodetAppManazer();
		manazer.vytvorStromy(20, 100, 100);

		actionChoiceBox.getItems().addAll(akcie);
		actionChoiceBox.setValue(akcie.get(0));

		pozemokChoiceBox.getItems().addAll(pozemky);
		pozemokChoiceBox.setValue(pozemky.get(0));
	}

	@FXML
	public void onSpustiButton() {

		String akcia = actionChoiceBox.getValue();
		String pozemok = pozemokChoiceBox.getValue();

		Integer supisneCislo = Integer.parseInt(supisneCisloText.getText());
		String popis = popisText.getText();

		Double lavaDolnaX = Double.parseDouble(this.lavaDolnaX.getText());
		Double lavaDolnaY = Double.parseDouble(this.lavaDolnaY.getText());
		Double pravaHornaX = Double.parseDouble(this.pravaHornaX.getText());
		Double pravaHornaY = Double.parseDouble(this.pravaHornaY.getText());

		String orientaciaSirkaDolna = this.orientaciaSirkaDolna.getText();
		String orientaciaVyskaDolna = this.orientaciaVyskaDolna.getText();
		String orientaciaSirkaHorna = this.orientaciaSirkaHorna.getText();
		String orientaciaVyskaHorna = this.orientaciaVyskaHorna.getText();

		GpsPozicia gpsPoziciaDolna = new GpsPozicia(orientaciaSirkaDolna, orientaciaVyskaDolna, lavaDolnaX, lavaDolnaY);
		GpsPozicia gpsPoziciaHorna = new GpsPozicia(orientaciaSirkaHorna, orientaciaVyskaHorna, pravaHornaX, pravaHornaY);
		Ohranicenie ohranicenie = new Ohranicenie(gpsPoziciaDolna, gpsPoziciaHorna);

		Nehnutelnost nehnutelnost = null;
		Parcela parcela = null;

		if (Objects.equals(pozemok, "Nehnuteľnosť")) {
			nehnutelnost = new Nehnutelnost(supisneCislo, popis, ohranicenie);
		} else if (Objects.equals(pozemok, "Parcela")) {
			parcela = new Parcela(supisneCislo, popis, ohranicenie);
		} else {
			throw new UnsupportedOperationException("Invalid pozemok");
		}

		var result = false;
		switch (akcia) {
			case "Pridať" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					result = manazer.pridajNehnutelnost(nehnutelnost);
				} else if (pozemok.equals("Parcela")) {
					result = manazer.pridajParcelu(parcela);
				}
			}
			case "Vymazať" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					result = manazer.vymazNehnutelnost(nehnutelnost);
				} else if (pozemok.equals("Parcela")) {
					result = manazer.vymazParcelu(parcela);
				}
			}
			case "Upraviť", "Nájsť" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					// manazer.upravNehnutelnost();
					throw new UnsupportedOperationException("Not implemented yet");
				} else if (pozemok.equals("Parcela")) {
					// manazer.upravParcelu();
					throw new UnsupportedOperationException("Not implemented yet");
				}
			}
			// manazer.najdiNehnutelnost();
			// manazer.najdiParcelu(

		}

		if (result) {
			resultLabel.setText("Pozemok bol spracovaný");
		} else {
			resultLabel.setText("Pozemok sa nepodarilo spracovať");
		}
	}

	public void onVypisNehnutelnostiButton() {
		nehnutelnostiListView.getItems().clear();
		nehnutelnostiListView.getItems().addAll(manazer.getNehnutelnosti().stream().map(IPozemok::toString).toList());
	}

	public void onVypisParcelyButton() {
		parcelyListView.getItems().clear();
		parcelyListView.getItems().addAll(manazer.getParcely().stream().map(IPozemok::toString).toList());
	}
}