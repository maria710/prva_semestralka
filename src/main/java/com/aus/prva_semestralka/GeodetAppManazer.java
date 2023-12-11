package com.aus.prva_semestralka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aus.prva_semestralka.fileManazer.Exporter;
import com.aus.prva_semestralka.fileManazer.Importer;
import com.aus.prva_semestralka.fileManazer.ImporterSubor;
import com.aus.prva_semestralka.generatory.Generator;
import com.aus.prva_semestralka.objekty.IData;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.QTNode;
import com.aus.prva_semestralka.struktury.QuadTree;

public class GeodetAppManazer {

	private QuadTree<Integer> nehnutelnosti;
	private QuadTree<Integer> parcely;
	private QTNode<Integer> aktualnyNodePriVyhladavani;

	private final Generator generator = new Generator();
	private final DynamickeHashovanieManazer dynamickeHashovanieManazer;

	public GeodetAppManazer() {
		dynamickeHashovanieManazer = new DynamickeHashovanieManazer();
	}

	public void vytvorStromy(int maxHlbka, int sirka, int dlzka) {
		nehnutelnosti = new QuadTree<>(maxHlbka, sirka, dlzka);
		parcely = new QuadTree<>(maxHlbka, sirka, dlzka);
	}

	public void vytvorSubory(int blokovaci, int blokovaciPreplnovaci, String nazovSuboruParcely, String nazovSuboruNehnutelnosti,
							 String preplnovaciSuborParcely, String preplnovaciSuborNehnutelnosti, int pocetBitov) {
		try {

			dynamickeHashovanieManazer.vytvorSubory(blokovaci, blokovaci, blokovaciPreplnovaci, blokovaciPreplnovaci,
													nazovSuboruParcely, nazovSuboruNehnutelnosti, preplnovaciSuborParcely, preplnovaciSuborNehnutelnosti, pocetBitov);
		} catch (Exception e) {
			throw new RuntimeException("Nepodarilo sa vytvorit subory pre dynamicke hashovanie");
		}
	}

	public List<IPozemok> getNehnutelnosti() {
		return filterAndCastToIPozemok(nehnutelnosti.getAllData());
	}

	public List<IPozemok> getParcely() {
		return filterAndCastToIPozemok(parcely.getAllData());
	}

	public boolean pridajNehnutelnost(Nehnutelnost nehnutelnost) {

		List<IPozemok> parcelyZoznam = filterAndCastToIPozemok(parcely.getAllData());
		var pridaneUspesne = pridajZavislostiNaPozemkochPreSubor(nehnutelnost, parcelyZoznam);
		if (nehnutelnost.getParcely().size() >= 6 || !pridaneUspesne) {
			return false;
		}
		if (dynamickeHashovanieManazer.pridajNehnutelnost(nehnutelnost)) {
			Nehnutelnost nehnutelnostPreStrom = new Nehnutelnost(nehnutelnost.getIdetifikacneCislo(), nehnutelnost.getGpsSuradnice());
			var result = nehnutelnosti.pridaj(nehnutelnostPreStrom);
			if (!result) {
				dynamickeHashovanieManazer.vymazNehnutelnost(nehnutelnost);
			} else {
				return true;
			}
		}
		return false;
	}

	public boolean pridajParcelu(Parcela parcela) {

		List<IPozemok> nehnutelnostiZoznam = filterAndCastToIPozemok(nehnutelnosti.getAllData());
		var pridaneUspesne = pridajZavislostiNaPozemkochPreSubor(parcela, nehnutelnostiZoznam);
		if (parcela.getNehnutelnosti().size() >= 5 || !pridaneUspesne) {
			return false;
		}
		if (dynamickeHashovanieManazer.pridajParcelu(parcela)) {
			Parcela parcelaPreStrom = new Parcela(parcela.getIdetifikacneCislo(), parcela.getGpsSuradnice());
			var result = parcely.pridaj(parcelaPreStrom);
			if (!result) {
				dynamickeHashovanieManazer.vymazParcelu(parcela);
			} else {
				return true;
			}
		}
		return false;
	}

	public void pridajParceluDoStromu(Parcela parcela) {
		parcely.pridaj(parcela);
	}

	public void pridajNehnutelnostDoStromu(Nehnutelnost nehnutelnost) {
		nehnutelnosti.pridaj(nehnutelnost);
	}

	public boolean vymazNehnutelnost(Nehnutelnost nehnutelnost) {
		var nehnutelnostNajdena = dynamickeHashovanieManazer.najdiNehnutelnost(nehnutelnost);
		nehnutelnostNajdena.getParcely().forEach(parcela -> {
			var parcelaNajdena = dynamickeHashovanieManazer.najdiParcelu(parcela);
			parcelaNajdena.odstranNehnutelnost(nehnutelnostNajdena);
			dynamickeHashovanieManazer.upravParcelu(parcelaNajdena);
		});

		if (dynamickeHashovanieManazer.vymazNehnutelnost(nehnutelnostNajdena)) {
			Nehnutelnost nehnutelnostPreStrom = new Nehnutelnost(nehnutelnost.getIdetifikacneCislo(), nehnutelnost.getGpsSuradnice());
			return nehnutelnosti.deleteDataFromNode(nehnutelnostPreStrom, aktualnyNodePriVyhladavani);
		}
		return false;
	}

	public boolean vymazParcelu(Parcela parcela) {
		var najdenaParcela = dynamickeHashovanieManazer.najdiParcelu(parcela);
		najdenaParcela.getNehnutelnosti().forEach(nehnutelnost -> {
			var nehnutelnostNajdena = dynamickeHashovanieManazer.najdiNehnutelnost(nehnutelnost);
			nehnutelnostNajdena.odstranParcelu(najdenaParcela);
			dynamickeHashovanieManazer.upravNehnutelnost(nehnutelnostNajdena);
		});
		if (dynamickeHashovanieManazer.vymazParcelu(najdenaParcela)) {
			Parcela parcelaPreStrom = new Parcela(najdenaParcela.getIdetifikacneCislo(), najdenaParcela.getGpsSuradnice());
			return parcely.deleteDataFromNode(parcelaPreStrom, aktualnyNodePriVyhladavani);
		}
		return false;
	}

	public List<IPozemok> najdiNehnutelnostiVOhraniceni(Ohranicenie ohranicenie) {
		var map = nehnutelnosti.findWithin(ohranicenie);
		aktualnyNodePriVyhladavani = map.getNode();
		return filterAndCastToIPozemok(map.getData());
	}

	public List<IPozemok> najdiParcelyVOhraniceni(Ohranicenie ohranicenie) {
		var map = parcely.findWithin(ohranicenie);
		aktualnyNodePriVyhladavani = map.getNode();
		return filterAndCastToIPozemok(map.getData());
	}

	public List<IPozemok> najdiParceluPodlaIdentifikacnehoCisla(Parcela parcela) {
		return Collections.singletonList(dynamickeHashovanieManazer.najdiParcelu(parcela));
	}

	public List<IPozemok> najdiNehnutelnostPodlaIdentifikacnehoCisla(Nehnutelnost nehnutelnost) {
		return Collections.singletonList(dynamickeHashovanieManazer.najdiNehnutelnost(nehnutelnost));
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

	private boolean pridajZavislostiNaPozemkochPreSubor(IPozemok pozemok, List<IPozemok> zavislosti) {
		ArrayList<IPozemok> prelinajuce = new ArrayList<>();
		for (IPozemok pozemok1 : zavislosti) {
			if (prelinajuSaPozemky(pozemok1, pozemok)) {
				prelinajuce.add(pozemok1);
			}
		}

		if (pozemok instanceof Nehnutelnost && prelinajuce.size() >= 6) {
			return false;
		}
		if (pozemok instanceof Parcela && prelinajuce.size() >= 5) {
			return false;
		}

		for (IPozemok pozemok1 : prelinajuce) {
			if (pozemok instanceof Nehnutelnost && pozemok1 instanceof Parcela) {
				var parcela = dynamickeHashovanieManazer.najdiParcelu((Parcela) pozemok1);
				if (parcela == null) {
					return false;
				}
				if (parcela.getNehnutelnosti().size() >= 5 || ((Nehnutelnost) pozemok).getParcely().size() >= 6) {
					return false;
				}
			}
			if (pozemok instanceof Parcela && pozemok1 instanceof Nehnutelnost) {
				var nehnutelnost = dynamickeHashovanieManazer.najdiNehnutelnost((Nehnutelnost) pozemok1);
				if (nehnutelnost.getParcely().size() >= 6 || ((Parcela) pozemok).getNehnutelnosti().size() >= 5) {
					return false;
				}
			}
		}

		for (IPozemok pozemok1 : prelinajuce) {
			if (pozemok instanceof Nehnutelnost && pozemok1 instanceof Parcela) {
				var parcela = dynamickeHashovanieManazer.najdiParcelu((Parcela) pozemok1);
				parcela.getNehnutelnosti().add((Nehnutelnost) pozemok);
				((Nehnutelnost) pozemok).getParcely().add(parcela);
			}
			if (pozemok instanceof Parcela && pozemok1 instanceof Nehnutelnost) {
				var nehnutelnost = dynamickeHashovanieManazer.najdiNehnutelnost((Nehnutelnost) pozemok1);
				((Parcela) pozemok).getNehnutelnosti().add(nehnutelnost);
				nehnutelnost.getParcely().add((Parcela) pozemok);
			}
		}
		return true;
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
		if (!Objects.equals(povodnyPozemok.getIdetifikacneCislo(), nehnutelnost.getIdetifikacneCislo())) {
			return false;
		}

		if (povodnyPozemok.getGpsSuradnice().equalsOhranicenie(nehnutelnost.getGpsSuradnice())) {
			if (dynamickeHashovanieManazer.upravNehnutelnost(nehnutelnost)) {
				return nehnutelnosti.uprav(povodnyPozemok, nehnutelnost, aktualnyNodePriVyhladavani);
			}
		} else {
			if (dynamickeHashovanieManazer.vymazNehnutelnost((Nehnutelnost) povodnyPozemok)) {
				nehnutelnosti.deleteDataFromNode(povodnyPozemok, aktualnyNodePriVyhladavani);
				if (dynamickeHashovanieManazer.pridajNehnutelnost(nehnutelnost)) {
					return nehnutelnosti.pridaj(nehnutelnost);
				}
			}
		}
		return false;
	}

	public boolean upravParcelu(IPozemok povodnyPozemok, Parcela parcela) {
		if (!Objects.equals(povodnyPozemok.getIdetifikacneCislo(), parcela.getIdetifikacneCislo())) {
			return false;
		}

		if (povodnyPozemok.getGpsSuradnice() == parcela.getGpsSuradnice()) {
			if (dynamickeHashovanieManazer.upravParcelu(parcela)) {
				return parcely.uprav(povodnyPozemok, parcela, aktualnyNodePriVyhladavani);
			}
		} else {
			if (dynamickeHashovanieManazer.vymazParcelu((Parcela) povodnyPozemok)) {
				parcely.deleteDataFromNode(povodnyPozemok, aktualnyNodePriVyhladavani);
				if (dynamickeHashovanieManazer.pridajParcelu(parcela)) {
					return parcely.pridaj(parcela);
				}
			}
		}
		return false;
	}

	public void importParcely(String absolutePath) {
		var parcelyImportovane = Importer.importFromCSVSimpleObject(absolutePath, true);
		for (IPozemok pozemok : parcelyImportovane) {
			pridajParceluDoStromu((Parcela) pozemok);
		}
	}

	public void importNehnutelnosti(String absolutePath) {
		var nehnutelnostiImportovane = Importer.importFromCSVSimpleObject(absolutePath, false);
		for (IPozemok pozemok : nehnutelnostiImportovane) {
			pridajNehnutelnostDoStromu((Nehnutelnost) pozemok);
		}
	}

	public void exportParcely(String absolutePath) {
		Exporter.exportToCSVSimpleObject(parcely.getAllData(), absolutePath);
	}

	public void exportNehnutelnosti(String absolutePath) {
		Exporter.exportToCSVSimpleObject(nehnutelnosti.getAllData(), absolutePath);
	}

	public void importSuborParcely(String absolutePath) {
		ImporterSubor importerSubor = new ImporterSubor();
		dynamickeHashovanieManazer.setHashovanieParcely(importerSubor.importDynamickeHashovanieParcela(absolutePath));
	}

	public void importSuborNehnutelnosti(String absolutePath) {
		ImporterSubor importerSubor = new ImporterSubor();
		dynamickeHashovanieManazer.setHashovanieNehnutelnosti(importerSubor.importDynamickeHashovanieNehnutelnosti(absolutePath));
	}

	public void exportSuborParcely(String absolutePath) {
		dynamickeHashovanieManazer.exportSuboruParcela(absolutePath);
	}

	public void exportSuborNehnutelnosti(String absolutePath) {
		dynamickeHashovanieManazer.exportSuboruNehnutelnost(absolutePath);
	}

	public void optimalizuj() {
		nehnutelnosti = nehnutelnosti.optimalizuj();
		parcely = parcely.optimalizuj();
	}

	public boolean zmenHlbku(int hlbka) {
		if (hlbka < 1) {
			return false;
		}
		return nehnutelnosti.zmenHlbku(hlbka) && parcely.zmenHlbku(hlbka);
	}

	private List<IPozemok> filterAndCastToIPozemok(List<IData<Integer>> dataList) {
		return dataList.stream()
					   .filter(data -> data instanceof IPozemok)
					   .map(data -> (IPozemok) data)
					   .collect(Collectors.toList());
	}

	public List<IPozemok> generujParcely(int pocetParciel) {
		var pozemky = generator.vygenerujPozemky(pocetParciel, parcely.getSirka(), parcely.getDlzka(), true);
		pozemky.forEach(pozemok -> pridajParcelu((Parcela) pozemok));
		return pozemky;
	}

	public List<IPozemok> generujNehnutelnosti(int pocetNehnutelnosti) {
		var pozemky = generator.vygenerujPozemky(pocetNehnutelnosti, nehnutelnosti.getSirka(), nehnutelnosti.getDlzka(), false);
		pozemky.forEach(pozemok -> pridajNehnutelnost((Nehnutelnost) pozemok));
		return pozemky;
	}

	public Double getZdraviePreParcely() {
		return Math.round(parcely.getZdravie() * 100.0) / 100.0;
	}

	public Double getZdraviePreNehnutelnosti() {
		return Math.round(nehnutelnosti.getZdravie() * 100.0) / 100.0;
	}

	public String toStringHlavnySuborParcely() {
		return dynamickeHashovanieManazer.toStringHlavnySuborParcely();
	}

	public String toStringPreplnovaciSuborParcely() {
		return dynamickeHashovanieManazer.toStringPreplnovaciSuborParcely();
	}

	public String toStringHlavnySuborNehnutelnosti() {
		return dynamickeHashovanieManazer.toStringHlavnySuborNehnutelnosti();
	}

	public String toStringPreplnovaciSuborNehnutelnosti() {
		return dynamickeHashovanieManazer.toStringPreplnovaciSuborNehnutelnosti();
	}

	public void closeSubory() {
		dynamickeHashovanieManazer.zavriSubory();
	}

	public void clearSubory() {
		dynamickeHashovanieManazer.clearSubory();
	}
}
