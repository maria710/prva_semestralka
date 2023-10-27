package com.aus.prva_semestralka;

import java.util.List;

import com.aus.prva_semestralka.fileManazer.Exporter;
import com.aus.prva_semestralka.fileManazer.Importer;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.QuadTree;

public class GeodetAppManazer {

	QuadTree nehnutelnosti;
	QuadTree parcely;

	public void vytvorStromy(int maxHlbka, int sirka, int dlzka) {
		nehnutelnosti = new QuadTree(maxHlbka, sirka, dlzka);
		parcely = new QuadTree(maxHlbka, sirka, dlzka);
	}

	public List<IPozemok> getNehnutelnosti() {
		return nehnutelnosti.getAllPozemky();
	}

	public List<IPozemok> getParcely() {
		return parcely.getAllPozemky();
	}

	public boolean pridajNehnutelnost(Nehnutelnost nehnutelnost) {

		List<IPozemok> parcelyZoznam = parcely.getAllPozemky();
		pridajZavislostiNaPozemkoch(nehnutelnost, parcelyZoznam);
		return nehnutelnosti.pridaj(nehnutelnost);
	}

	public boolean pridajParcelu(Parcela parcela) {

		List<IPozemok> nehnutelnostiZoznam = nehnutelnosti.getAllPozemky();
		pridajZavislostiNaPozemkoch(parcela, nehnutelnostiZoznam);
		return parcely.pridaj(parcela);
	}

	public boolean vymazNehnutelnost(Nehnutelnost nehnutelnost) {
		nehnutelnost.getParcely().forEach(parcela -> parcela.getNehnutelnosti().remove(nehnutelnost));
		return nehnutelnosti.deletePozemok(nehnutelnost);
	}

	public boolean vymazParcelu(Parcela parcela) {
		parcela.getNehnutelnosti().forEach(nehnutelnost -> nehnutelnost.getParcely().remove(parcela));
		return parcely.deletePozemok(parcela);
	}

	public List<IPozemok> najdiNehnutelnostiVOhraniceni(Ohranicenie ohranicenie) {
		return nehnutelnosti.findWithin(ohranicenie);
	}

	public List<IPozemok> najdiParcelyVOhraniceni(Ohranicenie ohranicenie) {
		return parcely.findWithin(ohranicenie);
	}

	private void pridajZavislostiNaPozemkoch(IPozemok pozemok, List<IPozemok> zavislosti) {
		for (IPozemok pozemok1 : zavislosti) {
			if (!prelinajuSaPozemky(pozemok1, pozemok)) {
				continue;
			}
			if (pozemok instanceof Nehnutelnost && pozemok1 instanceof Parcela) {
				((Parcela) pozemok1).getNehnutelnosti().add((Nehnutelnost) pozemok);
				((Nehnutelnost) pozemok).getParcely().add((Parcela) pozemok1);
			}
			if (pozemok instanceof Parcela && pozemok1 instanceof Nehnutelnost) {
				((Parcela) pozemok).getNehnutelnosti().add((Nehnutelnost) pozemok1);
				((Nehnutelnost) pozemok1).getParcely().add((Parcela) pozemok);
			}
		}
	}

	private boolean prelinajuSaPozemky(IPozemok nehnutelnost, IPozemok parcela) {
		var suradnica1 = nehnutelnost.getGpsSuradnice().getSuradnicaLavyDolny();
		var suradnica2 = nehnutelnost.getGpsSuradnice().getSuradnicaPravyHorny();

		var suradnica3 = parcela.getGpsSuradnice().getSuradnicaLavyDolny();
		var suradnica4 = parcela.getGpsSuradnice().getSuradnicaPravyHorny();

		double suradnicaX1 = suradnica1.getX();
		double suradnicaX2 = suradnica2.getX();
		double suradnicaY1 = suradnica1.getY();
		double suradnicaY2 = suradnica2.getY();

		double suradnicaX3 = suradnica3.getX();
		double suradnicaX4 = suradnica4.getX();
		double suradnicaY3 = suradnica3.getY();
		double suradnicaY4 = suradnica4.getY();

		// skontrolujeme ci sa prekryvaju na xovej osi
		if (suradnicaX2 < suradnicaX3 || suradnicaX4 < suradnicaX1) {
			return false;
		}
		// skontrolujeme ci sa prekryvaju na osi y
		if (suradnicaY1 > suradnicaY4 || suradnicaY3 > suradnicaY2) {
			return false;
		}

		return true;
	}

	public boolean upravNehnutelnost(IPozemok povodnyPozemok, Nehnutelnost nehnutelnost) {
		if (povodnyPozemok.getGpsSuradnice().equalsOhranicenie(nehnutelnost.getGpsSuradnice())) {
			return nehnutelnosti.uprav(povodnyPozemok, nehnutelnost);
		} else {
			nehnutelnosti.deletePozemok(povodnyPozemok);
			return nehnutelnosti.pridaj(nehnutelnost);
		}
	}

	public boolean upravParcelu(IPozemok povodnyPozemok, Parcela parcela) {
		if (povodnyPozemok.getGpsSuradnice() == parcela.getGpsSuradnice()) {
			return parcely.uprav(povodnyPozemok, parcela);
		} else {
			parcely.deletePozemok(povodnyPozemok);
			return parcely.pridaj(parcela);
		}
	}

	public void importParcely(String absolutePath) {
		var parcelyImportovane = Importer.importFromCSV(absolutePath, true);
		for (IPozemok pozemok : parcelyImportovane) {
			pridajParcelu((Parcela) pozemok);
		}
	}

	public void importNehnutelnosti(String absolutePath) {
		var nehnutelnostiImportovane = Importer.importFromCSV(absolutePath, false);
		for (IPozemok pozemok : nehnutelnostiImportovane) {
			pridajNehnutelnost((Nehnutelnost) pozemok);
		}
	}

	public void exportParcely(String absolutePath) {
		Exporter.exportToCSV(parcely.getAllPozemky(), absolutePath);
	}

	public void exportNehnutelnosti(String absolutePath) {
		Exporter.exportToCSV(nehnutelnosti.getAllPozemky(), absolutePath);
	}
}
