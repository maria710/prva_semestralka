package com.aus.prva_semestralka.fileManazer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;

public class Importer {

	public static List<IPozemok> importFromCSV(String csvFilePath, boolean isParcela) {
		List<IPozemok> pozemkyList = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
			reader.readLine(); // preskakujeme hlavicku v exceli

			String line;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split(";");
				if (data.length == 10) { // kontrola ci je riadok v spravnom formate
					int supisneCislo = Integer.parseInt(data[0]);
					String popis = data[1];
					String sirka1 = data[2];
					String dlzka1 = data[3];
					double x1 = Double.parseDouble(data[4]);
					double y1 = Double.parseDouble(data[5]);
					String sirka2 = data[6];
					String dlzka2 = data[7];
					double x2 = Double.parseDouble(data[8]);
					double y2 = Double.parseDouble(data[9]);

					Ohranicenie gpsPozicia = new Ohranicenie(new GpsPozicia(sirka1, dlzka1, x1, y1), new GpsPozicia(sirka2, dlzka2, x2, y2));

					if (isParcela) {
						Parcela parcela = new Parcela(supisneCislo, popis, gpsPozicia);
						pozemkyList.add(parcela);

					} else {
						Nehnutelnost nehnutelnost = new Nehnutelnost(supisneCislo, popis, gpsPozicia);
						pozemkyList.add(nehnutelnost);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Chyba pri citani zo suboru: " + csvFilePath);
		}

		return pozemkyList;
	}
}
