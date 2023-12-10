package com.aus.prva_semestralka;

import java.io.FileNotFoundException;

import com.aus.prva_semestralka.generatory.GeneratorKlucov;
import com.aus.prva_semestralka.objekty.IRecord;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.DynamickeHashovanie;

public class DynamickeHashovanieManazer {

	DynamickeHashovanie<Parcela> hashovanieParcely;
	DynamickeHashovanie<Nehnutelnost> hashovanieNehnutelnosti;

	GeneratorKlucov generatorKlucov = new GeneratorKlucov();

	public DynamickeHashovanieManazer() {
	}


	public void vytvorSubory(int blokovaciParcely, int blokovaciNehnutelnosti, int blokovaciParcelyPreplnovaci, int blokovaciNehnutelnostiPreplnovaci,
							 String nazovSuboruParcely, String nazovSuboruNehnutelnosti, String preplnovaciSuborParcely, String preplnovaciSuborNehnutelnosti) throws FileNotFoundException {

		hashovanieParcely = new DynamickeHashovanie<>(Parcela.class, blokovaciParcely, nazovSuboruParcely, preplnovaciSuborParcely, blokovaciParcelyPreplnovaci);
		hashovanieNehnutelnosti = new DynamickeHashovanie<>(Nehnutelnost.class, blokovaciNehnutelnosti, nazovSuboruNehnutelnosti, preplnovaciSuborNehnutelnosti, blokovaciNehnutelnostiPreplnovaci);
	}

	public String toStringHlavnySuborParcely() {
		return hashovanieParcely.toStringHlavny();
	}

	public String toStringPreplnovaciSuborParcely() {
		return hashovanieParcely.toStringPreplnovaci();
	}

	public String toStringHlavnySuborNehnutelnosti() {
		return hashovanieNehnutelnosti.toStringHlavny();
	}

	public String toStringPreplnovaciSuborNehnutelnosti() {
		return hashovanieNehnutelnosti.toStringPreplnovaci();
	}

	public boolean pridajParcelu(Parcela parcela) {
		parcela.setIdentifikacneCislo(generatorKlucov.getKluc());
		for (Nehnutelnost iPozemok : parcela.getNehnutelnosti()) {
			upravNehnutelnost(iPozemok);
		}
		return hashovanieParcely.insert(parcela);
	}

	public boolean pridajNehnutelnost(Nehnutelnost nehnutelnost) {
		nehnutelnost.setIdentifikacneCislo(generatorKlucov.getKluc());
		for (Parcela iPozemok : nehnutelnost.getParcely()) {
			upravParcelu(iPozemok);
		}
		return hashovanieNehnutelnosti.insert(nehnutelnost);
	}

	public boolean vymazParcelu(Parcela parcela) {
		skontrolujIdentifikacneCislo(parcela);
		return hashovanieParcely.delete(parcela);
	}

	public boolean vymazNehnutelnost(Nehnutelnost nehnutelnost) {
		skontrolujIdentifikacneCislo(nehnutelnost);
		return hashovanieNehnutelnosti.delete(nehnutelnost);
	}

	public Parcela najdiParcelu(Parcela parcela) {
		skontrolujIdentifikacneCislo(parcela);
		return (Parcela) hashovanieParcely.najdiZaznam(parcela);
	}

	public Nehnutelnost najdiNehnutelnost(Nehnutelnost nehnutelnost) {
		skontrolujIdentifikacneCislo(nehnutelnost);
		return (Nehnutelnost) hashovanieNehnutelnosti.najdiZaznam(nehnutelnost);
	}

	public boolean upravParcelu(Parcela parcela) {
		skontrolujIdentifikacneCislo(parcela);
		return hashovanieParcely.edit(parcela);
	}

	public boolean upravNehnutelnost(Nehnutelnost nehnutelnost) {
		skontrolujIdentifikacneCislo(nehnutelnost);
		return hashovanieNehnutelnosti.edit(nehnutelnost);
	}

	public void zavriSubory() {
		hashovanieParcely.close();
		hashovanieNehnutelnosti.close();
	}

	public void clearSubory() {
		hashovanieParcely.clear();
		hashovanieNehnutelnosti.clear();
	}

	private void skontrolujIdentifikacneCislo(IRecord record) {
		if (record.getIdetifikacneCislo() == null) {
			throw new RuntimeException("Pozemok nema identifikacne cislo");
		}
	}
}
