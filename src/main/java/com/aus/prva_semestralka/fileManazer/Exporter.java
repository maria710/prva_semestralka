package com.aus.prva_semestralka.fileManazer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.aus.prva_semestralka.objekty.IData;
import com.aus.prva_semestralka.objekty.IPozemok;

public class Exporter {

	public static void exportToCSV(List<IData> dataList, String csvFilePath) {
		List<IPozemok> pozemkyList = dataList.stream()
											 .filter(data -> data instanceof IPozemok)
											 .map(data -> (IPozemok) data)
											 .toList();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
			writer.write("SupisneCislo;Popis;Sirka1;Dlzka1;X1;Y1;Sirka2;Dlzka2;X2;Y2\n"); // prvy riadok v exceli

			for (IPozemok pozemok : pozemkyList) {
				String supisneCislo = pozemok.getSupisneCislo().toString();
				String popis = pozemok.getPopis();
				String sirka1 = pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getSirka();
				String dlzka1 = pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getVyska();
				String x1 = String.valueOf(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getX());
				String y1 = String.valueOf(pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getY());
				String sirka2 = pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getSirka();
				String dlzka2 = pozemok.getGpsSuradnice().getSuradnicaLavyDolny().getVyska();
				String x2 = String.valueOf(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getX());
				String y2 = String.valueOf(pozemok.getGpsSuradnice().getSuradnicaPravyHorny().getY());

				writer.write(supisneCislo + ";" + popis + ";" + sirka1 + ";" + dlzka1 + ";" + x1 + ";" + y1 + ";" + sirka2 + ";" + dlzka2 + ";" + x2 + ";" + y2 + "\n");
			}
		} catch (IOException e) {
			throw new RuntimeException("Chyba pri zapisovani do suboru: " + csvFilePath);
		}
	}
}

