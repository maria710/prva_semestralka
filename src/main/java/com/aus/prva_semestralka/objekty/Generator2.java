package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator2 {

	private final Random random = new Random();
	private final GeneratorKlucov generatorKlucov = new GeneratorKlucov();

	public IPozemok vygenerujNehnutelnost(int sirka, int dlzka) {
		return new Nehnutelnost(generatorKlucov.getKluc(), "Nehnutelnost", getRandomOhranicenie(sirka, dlzka));
	}

	public IPozemok vygenerujParcelu(int sirka, int dlzka) {
		return new Parcela(generatorKlucov.getKluc(), "Parcela", getRandomOhranicenie(sirka, dlzka));
	}

	public List<IPozemok> vygenerujPozemky(int pocet, int sirka, int dlzka, boolean isParcela) {
		ArrayList<IPozemok> pozemky = new ArrayList<>();
		if (isParcela) {
			for (int i = 0; i < pocet; i++) {
				pozemky.add(vygenerujParcelu(sirka, dlzka));
			}
		} else {
			for (int i = 0; i < pocet; i++) {
				pozemky.add(vygenerujNehnutelnost(sirka, dlzka));
			}
		}
		return pozemky;
	}

	public Ohranicenie getRandomOhranicenie(int sirka, int dlzka) {
		double suradnicaX1 = vygenerujVOhraniceni(0, sirka);
		double suradnicaY1 = vygenerujVOhraniceni(0, dlzka);
		double suradnicaX2 = vygenerujVOhraniceni(suradnicaX1, sirka);
		double suradnicaY2 = vygenerujVOhraniceni(suradnicaY1, dlzka);

		var randCislo = Math.random();
		if (randCislo < 0.9 && randCislo > 0.4) {
			suradnicaX1 /= 2;
			suradnicaX2 /= 2;
			suradnicaY1 /= 2;
			suradnicaY2 /= 2;
		} else if (randCislo < 0.3) {
			suradnicaX1 /= 4;
			suradnicaX2 /= 4;
			suradnicaY1 /= 4;
			suradnicaY2 /= 4;
		}

		return new Ohranicenie(new GpsPozicia("S", "Z", suradnicaX1, suradnicaY1), new GpsPozicia("J", "V", suradnicaX2, suradnicaY2));
	}

	private double vygenerujVOhraniceni(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
}
