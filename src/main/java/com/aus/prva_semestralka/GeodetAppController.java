package com.aus.prva_semestralka;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
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

	@FXML
	public MenuItem nehnutelnostiImportMenuItem;

	@FXML
	public MenuItem nehnutelnostiExportMenuItem;

	@FXML
	public MenuItem parcelaImportMenuItem;

	@FXML
	public MenuItem parcelyExportMenuItem;

	@FXML
	public Button zmenitRozmerButton;

	@FXML
	public Button optimalizovatButton;

	@FXML
	public TextField sirkaTextField;

	@FXML
	public TextField vyskaTextField;

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
	private ListView<IPozemok> parcelyListView;

	@FXML
	private ListView<IPozemok> nehnutelnostiListView;

	@FXML
	private Label resultLabel;

	@FXML
	private Label labelOfParcelyListView;

	@FXML
	private Label labelOfNehnutelnostiListView;

	@FXML
	private Button vymazatButton;

	private final List<String> akcie = List.of("Pridať", "Vymazať", "Upraviť", "Nájsť", "Vypísať");
	private final List<String> pozemky = List.of("Nehnuteľnosť", "Parcela", "Oba");

	private IPozemok povodnyPozemok;

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
		}

		if (!Objects.equals(akcia, "Nájsť") && Objects.equals(pozemok, "Oba")) {
			resultLabel.setText("Operácia nie je podporovaná pre oba pozemky");
			return;
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
					var vysledneNehnutelnosti = manazer.najdiNehnutelnostiVOhraniceni(ohranicenie);
					labelOfNehnutelnostiListView.setText("Nehnuteľnosti na vymazanie vo zvolenom ohraničení - označ pre odstránenie");
					refreshNehnutelnostiView(vysledneNehnutelnosti);
				} else if (pozemok.equals("Parcela")) {
					var vysledneParcely = manazer.najdiParcelyVOhraniceni(ohranicenie);
					labelOfParcelyListView.setText("Parcely na vymazanie vo zvolenom ohraničení - označ pre odstránenie");
					parcelyListView.getItems().clear();
					refreshParcelyView(vysledneParcely);
				}
				vymazatButton.setDisable(false);
			}
			case "Nájsť" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					var vysledneNehnutelnosti = manazer.najdiNehnutelnostiVOhraniceni(ohranicenie);
					labelOfNehnutelnostiListView.setText("Nájdené nehnuteľnosti vo zvolenom ohraničení");
					refreshNehnutelnostiView(vysledneNehnutelnosti);
				} else if (pozemok.equals("Parcela")) {
					var vysledneParcely = manazer.najdiParcelyVOhraniceni(ohranicenie);
					labelOfParcelyListView.setText("Nájdené parcely vo zvolenom ohraničení");
					parcelyListView.getItems().clear();
					refreshParcelyView(vysledneParcely);
				} else {
					var vysledneNehnutelnosti = manazer.najdiNehnutelnostiVOhraniceni(ohranicenie);
					var vysledneParcely = manazer.najdiParcelyVOhraniceni(ohranicenie);
					vysledneParcely.addAll(vysledneNehnutelnosti);
					labelOfParcelyListView.setText("Nájdené parcely a nehnuteľnosti vo zvolenom ohraničení");
					parcelyListView.getItems().clear();
					refreshParcelyView(vysledneParcely);
				}
			}
			case "Upraviť" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					result = manazer.upravNehnutelnost(povodnyPozemok, nehnutelnost);
				} else if (pozemok.equals("Parcela")) {
					result = manazer.upravParcelu(povodnyPozemok, parcela);
				}
			}
			case "Vypísať" -> {
				if (pozemok.equals("Nehnuteľnosť")) {
					var vysledneNehnutelnosti = manazer.najdiNehnutelnostiVOhraniceni(ohranicenie);
					labelOfNehnutelnostiListView.setText("Nájdené nehnuteľnosti vo zvolenom ohraničení");
					refreshNehnutelnostiView(vysledneNehnutelnosti);
					nehnutelnostiListView.setOnMouseClicked(event -> fillFieldsWith(nehnutelnostiListView.getSelectionModel().getSelectedItem()));
				} else if (pozemok.equals("Parcela")) {
					var vysledneParcely = manazer.najdiParcelyVOhraniceni(ohranicenie);
					labelOfParcelyListView.setText("Nájdené parcely vo zvolenom ohraničení");
					parcelyListView.getItems().clear();
					refreshParcelyView(vysledneParcely);
					parcelyListView.setOnMouseClicked(event -> fillFieldsWith(parcelyListView.getSelectionModel().getSelectedItem()));
				}
			}
		}

		if (result) {
			resultLabel.setText("Pozemok bol spracovaný");
		} else {
			resultLabel.setText("Pozemok sa nepodarilo spracovať");
		}
	}

	public void onVypisNehnutelnostiButton() {
		setCellFactoryFor(nehnutelnostiListView);

		labelOfNehnutelnostiListView.setText("Nehnuteľnosti");
		refreshNehnutelnostiView(manazer.getNehnutelnosti());
		vymazatButton.setDisable(true);
	}

	public void onVypisParcelyButton() {
		setCellFactoryFor(parcelyListView);

		labelOfParcelyListView.setText("Parcely");
		refreshParcelyView(manazer.getParcely());
		vymazatButton.setDisable(true);
	}

	public void onVymazatButton() {
		IPozemok pozemok = nehnutelnostiListView.getSelectionModel().getSelectedItem();
		if (pozemok == null) {
			pozemok = parcelyListView.getSelectionModel().getSelectedItem();
		}
		if (pozemok != null) {
			if (pozemok instanceof Nehnutelnost) {
				manazer.vymazNehnutelnost((Nehnutelnost) pozemok);
				refreshNehnutelnostiView(manazer.getNehnutelnosti());
			} else if (pozemok instanceof Parcela) {
				manazer.vymazParcelu((Parcela) pozemok);
				refreshParcelyView(manazer.getParcely());
			}
		}

	}

	private void setCellFactoryFor(ListView<IPozemok> listView) {
		listView.setCellFactory(param -> new ListCell<>() {

			@Override
			protected void updateItem(IPozemok item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.toString());
				}
			}
		});
	}

	private void refreshNehnutelnostiView(List<IPozemok> nehnutelnosti) {
		nehnutelnostiListView.getItems().clear();
		nehnutelnostiListView.getItems().addAll(nehnutelnosti);
	}

	private void refreshParcelyView(List<IPozemok> parcely) {
		parcelyListView.getItems().clear();
		parcelyListView.getItems().addAll(parcely);
	}

	private void fillFieldsWith(IPozemok pozemok) {
		povodnyPozemok = pozemok;
		supisneCisloText.setText(pozemok.getSupisneCislo().toString());
		popisText.setText(pozemok.getPopis());
		lavaDolnaX.setText(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getX().toString());
		lavaDolnaY.setText(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getY().toString());
		pravaHornaX.setText(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getX().toString());
		pravaHornaY.setText(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getY().toString());
		orientaciaSirkaDolna.setText(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getSirka());
		orientaciaVyskaDolna.setText(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getVyska());
		orientaciaSirkaHorna.setText(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getSirka());
		orientaciaVyskaHorna.setText(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getVyska());
	}

	public void onNehnutelnostiImportClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("C:\\Users\\mkuruczova\\projects\\aus2"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			manazer.importNehnutelnosti(file.getAbsolutePath());
		}
		refreshNehnutelnostiView(manazer.getNehnutelnosti());
	}

	public void onParcelyImportClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("C:\\Users\\mkuruczova\\projects\\aus2"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			manazer.importParcely(file.getAbsolutePath());
		}
		refreshParcelyView(manazer.getParcely());
	}

	public void onNehnutelnostiExportClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("C:\\Users\\mkuruczova\\projects\\aus2"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showSaveDialog(null);
		if (file != null) {
			manazer.exportNehnutelnosti(file.getAbsolutePath());
		}
	}

	public void onParcelyExportClick() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("C:\\Users\\mkuruczova\\projects\\aus2"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showSaveDialog(null);
		if (file != null) {
			manazer.exportParcely(file.getAbsolutePath());
		}
	}

	public void onOptimalizovatClick() {
		manazer.optimalizuj();
	}

	public void onZmenitRozmerClick() {
		int sirka = Integer.parseInt(sirkaTextField.getText());
		int vyska = Integer.parseInt(vyskaTextField.getText());
		manazer.zmenRozmer(sirka, vyska);
	}
}